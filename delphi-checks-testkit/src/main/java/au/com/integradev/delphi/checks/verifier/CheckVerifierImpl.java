/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.checks.verifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.antlr.ast.visitors.SymbolAssociationVisitor;
import au.com.integradev.delphi.builders.DelphiTestFile;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.check.DelphiCheckContextImpl;
import au.com.integradev.delphi.check.MasterCheckRegistrar;
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.preprocessor.directive.CompilerDirectiveParserImpl;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.symbol.SymbolTable;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.utils.DelphiUtils;
import com.google.common.base.Splitter;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.batch.sensor.issue.IssueLocation;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleScope;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;

/**
 * Based loosely on {@code InternalCheckVerifier} from the sonar-java project.
 *
 * @see <a
 *     href="https://github.com/SonarSource/sonar-java/blob/master/java-checks-testkit/src/main/java/org/sonar/java/checks/verifier/internal/InternalCheckVerifier.java">
 *     InternalCheckVerifier </a>
 */
public class CheckVerifierImpl implements CheckVerifier {
  private static final Logger LOG = LoggerFactory.getLogger(CheckVerifierImpl.class);
  private static final Pattern NONCOMPLIANT_PATTERN =
      Pattern.compile("(?i)^//\\s*Noncompliant(?:@([-+]\\d+))?\\b");

  private DelphiCheck check;
  private DelphiTestFile testFile;
  private final Set<String> unitScopeNames = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
  private final Map<String, String> unitAliases = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  private final List<DelphiTestUnitBuilder> searchPathUnits = new ArrayList<>();
  private final List<DelphiTestUnitBuilder> standardLibraryUnits = new ArrayList<>();

  @Override
  public CheckVerifier withCheck(DelphiCheck check) {
    requireUnassigned(this.check, "check");
    this.check = check;
    return this;
  }

  @Override
  public CheckVerifier withUnitScopeName(String unitScopeName) {
    unitScopeNames.add(unitScopeName);
    return this;
  }

  @Override
  public CheckVerifier withUnitAlias(String alias, String unitName) {
    unitAliases.put(alias, unitName);
    return this;
  }

  @Override
  public CheckVerifier withSearchPathUnit(DelphiTestUnitBuilder builder) {
    searchPathUnits.add(builder);
    return this;
  }

  @Override
  public CheckVerifier withStandardLibraryUnit(DelphiTestUnitBuilder builder) {
    standardLibraryUnits.add(builder);
    return this;
  }

  @Override
  public CheckVerifier onFile(DelphiTestFile builder) {
    requireUnassigned(this.testFile, "file");
    this.testFile = builder;
    return this;
  }

  @Override
  public void verifyIssues() {

    List<Issue> issues = execute();

    if (issues.isEmpty()) {
      throw new AssertionError("No issue raised. At least one issue expected");
    }

    List<Integer> expected =
        testFile.delphiFile().getComments().stream()
            .map(
                c -> {
                  Matcher matcher = NONCOMPLIANT_PATTERN.matcher(c.getImage());
                  if (!matcher.matches()) {
                    return Optional.<Integer>empty();
                  }

                  var matchResult = matcher.toMatchResult();
                  String offset = matchResult.group(1);
                  if (offset == null) {
                    return Optional.of(c.getBeginLine());
                  }

                  try {
                    return Optional.of(c.getBeginLine() + Integer.parseInt(offset));
                  } catch (NumberFormatException e) {
                    throw new AssertionError(
                        String.format(
                            "Failed to parse 'Noncompliant' comment line offset '%s' as an"
                                + " integer.",
                            offset));
                  }
                })
            .flatMap(Optional::stream)
            .collect(Collectors.toList());

    verifyIssuesOnLinesInternal(issues, expected);
  }

  private static void verifyIssuesOnLinesInternal(List<Issue> issues, List<Integer> expected) {
    List<Integer> unexpectedLines = new ArrayList<>();

    for (Issue issue : issues) {
      IssueLocation issueLocation = issue.primaryLocation();

      TextRange textRange = issueLocation.textRange();
      if (textRange == null) {
        throw new AssertionError(
            String.format(
                "Expected issues to be raised at line level, not at %s level",
                issueLocation.inputComponent().isFile() ? "file" : "project"));
      }

      Integer line = textRange.start().line();
      if (!expected.remove(line)) {
        unexpectedLines.add(line);
      }
    }

    if (!expected.isEmpty() || !unexpectedLines.isEmpty()) {
      StringBuilder message = new StringBuilder("Issues were ");
      if (!expected.isEmpty()) {
        message.append("expected at ").append(expected);
      }
      if (!expected.isEmpty() && !unexpectedLines.isEmpty()) {
        message.append(", ");
      }
      if (!unexpectedLines.isEmpty()) {
        message.append("unexpected at ").append(unexpectedLines);
      }
      throw new AssertionError(message.toString());
    }
  }

  @Override
  public void verifyIssueOnFile() {
    List<Issue> issues = execute();

    IssueLocation issueLocation = verifySingleIssueOnComponent(issues, "file");

    if (!issueLocation.inputComponent().isFile()) {
      throw new AssertionError(
          "Expected the issue to be raised at file level, not at project level");
    }
  }

  @Override
  public void verifyIssueOnProject() {
    List<Issue> issues = execute();

    IssueLocation issueLocation = verifySingleIssueOnComponent(issues, "project");

    if (issueLocation.inputComponent().isFile()) {
      throw new AssertionError(
          "Expected the issue to be raised at project level, not at file level");
    }
  }

  @Override
  public void verifyNoIssues() {
    List<Issue> issues = execute();
    if (!issues.isEmpty()) {
      throw new AssertionError(
          String.format(
              "No issues expected but got %d issues:%s", issues.size(), issuesToString(issues)));
    }
  }

  private List<Issue> execute() {
    requireAssigned(check, "check");
    requireAssigned(testFile, "file");

    List<String> lines = Splitter.on('\n').splitToList(testFile.sourceCode());
    for (int lineNum = 0; lineNum < lines.size(); ++lineNum) {
      String printLine = String.format("%03d %s", lineNum + 1, lines.get(lineNum));
      LOG.info(printLine);
    }

    Path standardLibraryPath = createStandardLibrary();

    DelphiInputFile file = testFile.delphiFile();
    SymbolTable symbolTable =
        SymbolTable.builder()
            .preprocessorFactory(new DelphiPreprocessorFactory(Platform.WINDOWS))
            .typeFactory(
                new TypeFactoryImpl(
                    DelphiProperties.COMPILER_TOOLCHAIN_DEFAULT,
                    DelphiProperties.COMPILER_VERSION_DEFAULT))
            .standardLibraryPath(standardLibraryPath)
            .sourceFiles(List.of(file.getSourceCodeFile().toPath()))
            .unitAliases(unitAliases)
            .unitScopeNames(unitScopeNames)
            .searchPath(createSearchPath())
            .build();

    FileUtils.deleteQuietly(standardLibraryPath.toFile());

    new SymbolAssociationVisitor()
        .visit(file.getAst(), new SymbolAssociationVisitor.Data(symbolTable));

    var sensorContext = SensorContextTester.create(FileUtils.getTempDirectory());
    sensorContext.settings().setProperty(DelphiProperties.TEST_TYPE_KEY, "Test.TTestSuite");

    var compilerDirectiveParser = new CompilerDirectiveParserImpl(Platform.WINDOWS);

    var checkRegistrar = mock(MasterCheckRegistrar.class);
    when(checkRegistrar.getRuleKey(check))
        .thenReturn(Optional.of(RuleKey.of("test", check.getClass().getSimpleName())));
    when(checkRegistrar.getScope(check)).thenReturn(RuleScope.ALL);

    DelphiCheckContext context =
        new DelphiCheckContextImpl(
            check, sensorContext, file, compilerDirectiveParser, checkRegistrar);

    check.start(context);
    check.visit(file.getAst(), context);
    check.end(context);

    return List.copyOf(sensorContext.allIssues());
  }

  private static IssueLocation verifySingleIssueOnComponent(List<Issue> issues, String component) {
    if (issues.size() != 1) {
      throw new AssertionError(
          "A single issue is expected on the "
              + component
              + ", but "
              + (issues.isEmpty()
                  ? "none has been raised."
                  : String.format("%d issues have been raised.", issues.size())));
    }

    IssueLocation issueLocation = issues.get(0).primaryLocation();
    TextRange textRange = issueLocation.textRange();

    if (textRange != null) {
      throw new AssertionError(
          String.format(
              "Expected an issue directly on %s but was raised on line %d",
              component, textRange.start().line()));
    }

    return issueLocation;
  }

  private static void requireUnassigned(@Nullable Object object, String fieldName) {
    if (object != null) {
      throw new AssertionError(String.format("Do not set %s multiple times!", fieldName));
    }
  }

  private static void requireAssigned(@Nullable Object object, String fieldName) {
    if (object == null) {
      throw new AssertionError(
          String.format("Set %s before calling any verification method!", fieldName));
    }
  }

  private static String issuesToString(List<Issue> issues) {
    if (issues.isEmpty()) {
      return "<no issues>";
    }

    return issues.stream()
        .map(Issue::primaryLocation)
        .sorted(CheckVerifierImpl::compareIssueLocations)
        .map(CheckVerifierImpl::issueLocationToString)
        .collect(Collectors.joining("\n--> ", "\n--> ", ""));
  }

  private static int compareIssueLocations(IssueLocation l1, IssueLocation l2) {
    if (l1.textRange() == null) {
      return 1;
    }

    if (l2.textRange() == null) {
      return -1;
    }

    return Integer.compare(
        Objects.requireNonNull(l1.textRange()).start().line(),
        Objects.requireNonNull(l2.textRange()).start().line());
  }

  private static String issueLocationToString(IssueLocation location) {
    TextRange textRange = location.textRange();
    return String.format(
        "'%s' in %s%s",
        location.message(),
        location.inputComponent(),
        (textRange == null ? "" : (":" + textRange.start().line())));
  }

  private Path createStandardLibrary() {
    try {
      Path path = Files.createTempDirectory("bds_source");
      Files.writeString(
          path.resolve("SysInit.pas"), "unit SysInit;\ninterface\nimplementation\nend.");

      Files.writeString(
          path.resolve("System.pas"),
          "unit System;\n"
              + "\n"
              + "interface\n"
              + "\n"
              + "type\n"
              + "  TArray<T> = array of T;\n"
              + "\n"
              + "  TObject = class;\n"
              + "\n"
              + "  TClass = class of TObject;\n"
              + "\n"
              + "  PMethod = ^TMethod;\n"
              + "  TMethod = record\n"
              + "    Code, Data: Pointer;\n"
              + "  public\n"
              + "    class operator Equal(const Left, Right: TMethod): Boolean;\n"
              + "    class operator NotEqual(const Left, Right: TMethod): Boolean;\n"
              + "    class operator GreaterThan(const Left, Right: TMethod): Boolean;\n"
              + "    class operator GreaterThanOrEqual(const Left, Right: TMethod): Boolean;\n"
              + "    class operator LessThan(const Left, Right: TMethod): Boolean;\n"
              + "    class operator LessThanOrEqual(const Left, Right: TMethod): Boolean;\n"
              + "  end;\n"
              + "\n"
              + "  TObject = class\n"
              + "  public\n"
              + "    constructor Create;\n"
              + "    procedure Free;\n"
              + "    class function InitInstance(Instance: Pointer): TObject;\n"
              + "    procedure CleanupInstance;\n"
              + "    function ClassType: TClass; inline;\n"
              + "    class function ClassName: string;\n"
              + "    class function ClassNameIs(const Name: string): Boolean;\n"
              + "    class function ClassParent: TClass;\n"
              + "    class function ClassInfo: Pointer; inline;\n"
              + "    class function InstanceSize: Longint; inline;\n"
              + "    class function InheritsFrom(AClass: TClass): Boolean;\n"
              + "    class function MethodAddress(const Name: _ShortStr): Pointer; overload;\n"
              + "    class function MethodAddress(const Name: string): Pointer; overload;\n"
              + "    class function MethodName(Address: Pointer): string;\n"
              + "    class function QualifiedClassName: string;\n"
              + "    function FieldAddress(const Name: _ShortStr): Pointer; overload;\n"
              + "    function FieldAddress(const Name: string): Pointer; overload;\n"
              + "    function GetInterface(const IID: TGUID; out Obj): Boolean;\n"
              + "    class function GetInterfaceEntry(const IID: TGUID): PInterfaceEntry;\n"
              + "    class function GetInterfaceTable: PInterfaceTable;\n"
              + "    class function UnitName: string;\n"
              + "    class function UnitScope: string;\n"
              + "    function Equals(Obj: TObject): Boolean; virtual;\n"
              + "    function GetHashCode: Integer; virtual;\n"
              + "    function ToString: string; virtual;\n"
              + "    function SafeCallException(ExceptObject: TObject;\n"
              + "      ExceptAddr: Pointer): HResult; virtual;\n"
              + "    procedure AfterConstruction; virtual;\n"
              + "    procedure BeforeDestruction; virtual;\n"
              + "    procedure Dispatch(var Message); virtual;\n"
              + "    procedure DefaultHandler(var Message); virtual;\n"
              + "    class function NewInstance: TObject; virtual;\n"
              + "    procedure FreeInstance; virtual;\n"
              + "    destructor Destroy; virtual;\n"
              + "  end;\n"
              + "\n"
              + "  IInterface = interface\n"
              + "    function QueryInterface(const IID: TGUID; out Obj): HResult; stdcall;\n"
              + "    function _AddRef: Integer; stdcall;\n"
              + "    function _Release: Integer; stdcall;\n"
              + "  end;\n"
              + "\n"
              + "  IEnumerator = interface(IInterface)\n"
              + "    function GetCurrent: TObject;\n"
              + "    function MoveNext: Boolean;\n"
              + "    procedure Reset;\n"
              + "    property Current: TObject read GetCurrent;\n"
              + "  end;\n"
              + "\n"
              + "  IEnumerable = interface(IInterface)\n"
              + "    function GetEnumerator: IEnumerator;\n"
              + "  end;\n"
              + "\n"
              + "  IEnumerator<T> = interface(IEnumerator)\n"
              + "    function GetCurrent: T;\n"
              + "    property Current: T read GetCurrent;\n"
              + "  end;\n"
              + "\n"
              + "  IEnumerable<T> = interface(IEnumerable)\n"
              + "    function GetEnumerator: IEnumerator<T>;\n"
              + "  end;\n"
              + "\n"
              + "  IComparable = interface(IInterface)\n"
              + "    function CompareTo(Obj: TObject): Integer;\n"
              + "  end;\n"
              + "\n"
              + "  IComparable<T> = interface(IComparable)\n"
              + "    function CompareTo(Value: T): Integer;\n"
              + "  end;\n"
              + "\n"
              + "  IEquatable<T> = interface(IInterface)\n"
              + "    function Equals(Value: T): Boolean;\n"
              + "  end;\n"
              + "\n"
              + "  PVarRec = ^TVarRec;\n"
              + "  TVarRec = record\n"
              + "    case Integer of\n"
              + "      0: (case Byte of\n"
              + "            vtInteger:       (VInteger: Integer);\n"
              + "            vtBoolean:       (VBoolean: Boolean);\n"
              + "            vtChar:          (VChar: _AnsiChr);\n"
              + "            vtExtended:      (VExtended: PExtended);\n"
              + "            vtString:        (VString: _PShortStr);\n"
              + "            vtPointer:       (VPointer: Pointer);\n"
              + "            vtPChar:         (VPChar: _PAnsiChr);\n"
              + "            vtObject:        (VObject: TObject);\n"
              + "            vtClass:         (VClass: TClass);\n"
              + "            vtWideChar:      (VWideChar: WideChar);\n"
              + "            vtPWideChar:     (VPWideChar: PWideChar);\n"
              + "            vtAnsiString:    (VAnsiString: Pointer);\n"
              + "            vtCurrency:      (VCurrency: PCurrency);\n"
              + "            vtVariant:       (VVariant: PVariant);\n"
              + "            vtInterface:     (VInterface: Pointer);\n"
              + "            vtWideString:    (VWideString: Pointer);\n"
              + "            vtInt64:         (VInt64: PInt64);\n"
              + "            vtUnicodeString: (VUnicodeString: Pointer);\n"
              + "         );\n"
              + "      1: (_Reserved1: NativeInt;\n"
              + "          VType:      Byte;\n"
              + "         );\n"
              + "  end;\n"
              + "\n"
              + "  TInterfacedObject = class(TObject, IInterface)\n"
              + "  protected\n"
              + "    FRefCount: Integer;\n"
              + "    function QueryInterface(const IID: TGUID; out Obj): HResult; stdcall;\n"
              + "    function _AddRef: Integer; stdcall;\n"
              + "    function _Release: Integer; stdcall;\n"
              + "  public\n"
              + "    procedure AfterConstruction; override;\n"
              + "    procedure BeforeDestruction; override;\n"
              + "    class function NewInstance: TObject; override;\n"
              + "    property RefCount: Integer read FRefCount;\n"
              + "  end;\n"
              + "\n"
              + "  TInterfacedClass = class of TInterfacedObject;\n"
              + "\n"
              + "  TClassHelperBase = class(TInterfacedObject, IInterface)\n"
              + "  protected\n"
              + "    FInstance: TObject;\n"
              + "    constructor _Create(Instance: TObject);\n"
              + "  end;\n"
              + "\n"
              + "  TClassHelperBaseClass = class of TClassHelperBase;\n"
              + "\n"
              + "  TCustomAttribute = class(TObject)\n"
              + "  end;\n"
              + "\n"
              + "implementation\n"
              + "\n"
              + "end.");

      Files.writeString(
          path.resolve("System.Classes.pas"),
          "unit System.Classes;\n"
              + "\n"
              + "interface\n"
              + "\n"
              + "type\n"
              + "  TPersistent = class(TObject)\n"
              + "\n"
              + "  end;\n"
              + "\n"
              + "  TComponentClass = class of TComponent;\n"
              + "\n"
              + "  TComponent = class(TPersistent, IInterface)\n"
              + " \n"
              + "  end;\n"
              + "\n"
              + "implementation\n"
              + "\n"
              + "end.");

      for (DelphiTestUnitBuilder unit : standardLibraryUnits) {
        Path source = unit.delphiFile().getSourceCodeFile().toPath();
        Path target = path.resolve(source.getFileName().toString());
        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
      }

      return path;
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private SearchPath createSearchPath() {
    return SearchPath.create(
        searchPathUnits.stream()
            .map(builder -> builder.delphiFile().getInputFile())
            .map(DelphiUtils::inputFileToPath)
            .map(Path::getParent)
            .collect(Collectors.toUnmodifiableList()));
  }
}
