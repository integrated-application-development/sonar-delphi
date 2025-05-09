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
import au.com.integradev.delphi.antlr.DelphiFileStream;
import au.com.integradev.delphi.antlr.ast.visitors.SymbolAssociationVisitor;
import au.com.integradev.delphi.builders.DelphiTestFile;
import au.com.integradev.delphi.builders.DelphiTestUnitBuilder;
import au.com.integradev.delphi.check.MasterCheckRegistrar;
import au.com.integradev.delphi.compiler.CompilerVersion;
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.compiler.Toolchain;
import au.com.integradev.delphi.file.DelphiFile;
import au.com.integradev.delphi.file.DelphiFile.DelphiInputFile;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.preprocessor.directive.CompilerDirectiveParserImpl;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.reporting.TextRangeReplacement;
import au.com.integradev.delphi.reporting.edits.QuickFixEditImpl;
import au.com.integradev.delphi.symbol.SymbolTable;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.utils.DelphiUtils;
import com.google.common.base.Splitter;
import com.google.common.base.Suppliers;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Supplier;
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
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;

/**
 * Based loosely on {@code InternalCheckVerifier} from the sonar-java project.
 *
 * @see <a
 *     href="https://github.com/SonarSource/sonar-java/blob/master/java-checks-testkit/src/main/java/org/sonar/java/checks/verifier/internal/InternalCheckVerifier.java">
 *     InternalCheckVerifier </a>
 */
public class CheckVerifierImpl implements CheckVerifier {
  private static final Logger LOG = LoggerFactory.getLogger(CheckVerifierImpl.class);

  private DelphiCheck check;
  private DelphiTestFile testFile;
  private CompilerVersion compilerVersion = DelphiProperties.COMPILER_VERSION_DEFAULT;
  private Toolchain toolchain = DelphiProperties.COMPILER_TOOLCHAIN_DEFAULT;
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
  public CheckVerifier withCompilerVersion(CompilerVersion compilerVersion) {
    this.compilerVersion = compilerVersion;
    return this;
  }

  @Override
  public CheckVerifier withToolchain(Toolchain toolchain) {
    this.toolchain = toolchain;
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
    ExecutionResult result = execute();
    List<Issue> issues = result.getIssues();
    List<QuickFix> quickFixes = result.getQuickFixes();

    if (issues.isEmpty()) {
      throw new AssertionError("No issue raised. At least one issue expected");
    }

    Expectations expected = Expectations.fromComments(testFile.delphiFile());

    verifyIssuesOnLinesInternal(issues, expected.issues());
    if (!expected.quickFixes().isEmpty()) {
      assertQuickFixes(quickFixes, expected.quickFixes());
    }
  }

  private static void verifyIssuesOnLinesInternal(
      List<Issue> issues, List<IssueExpectation> expectedIssues) {
    List<IssueExpectation> unexpectedIssues = new ArrayList<>();
    List<IssueExpectation> expectations = new ArrayList<>(expectedIssues);

    for (Issue issue : issues) {
      Optional<IssueExpectation> expectedIssue = findIssue(issue, expectations);
      if (expectedIssue.isPresent()) {
        expectations.remove(expectedIssue.get());
      } else {
        unexpectedIssues.add(expectationFromIssue(issue));
      }
    }

    assertIssueMismatchesEmpty(expectations, unexpectedIssues);
  }

  private static IssueExpectation expectationFromIssue(Issue issue) {
    int primaryLine = getStartingLine(issue.primaryLocation());
    List<List<Integer>> actualLines =
        issue.flows().stream()
            .map(
                flow ->
                    flow.locations().stream()
                        .map(location -> getStartingLine(location) - primaryLine)
                        .collect(Collectors.toList()))
            .sorted(Comparator.comparing(list -> list.get(0)))
            .collect(Collectors.toList());
    return new IssueExpectation(primaryLine, actualLines);
  }

  private static Optional<IssueExpectation> findIssue(
      Issue issue, List<IssueExpectation> expectations) {
    int line = getStartingLine(issue.primaryLocation());

    for (IssueExpectation expectation : expectations) {
      if (expectation.getBeginLine() != line
          || expectation.getFlowLines().size() != issue.flows().size()) {
        continue;
      }
      List<List<Integer>> expectedLines =
          expectation.getFlowLines().stream()
              .map(
                  offsets ->
                      offsets.stream().map(offset -> offset + line).collect(Collectors.toList()))
              .collect(Collectors.toList());
      List<List<Integer>> actualLines =
          issue.flows().stream()
              .map(
                  flow ->
                      flow.locations().stream()
                          .map(CheckVerifierImpl::getStartingLine)
                          .collect(Collectors.toList()))
              .collect(Collectors.toList());
      if (expectedLines.equals(actualLines)) {
        return Optional.of(expectation);
      }
    }
    return Optional.empty();
  }

  private static int getStartingLine(IssueLocation location) {
    TextRange textRange = location.textRange();
    if (textRange == null) {
      throw new AssertionError(
          String.format(
              "Expected issues to be raised at line level, not at %s level",
              location.inputComponent().isFile() ? "file" : "project"));
    }
    return textRange.start().line();
  }

  private void assertQuickFixes(
      List<QuickFix> actualQuickFixes, List<QuickFixExpectation> expectedQuickFixes) {
    if (expectedQuickFixes.size() != actualQuickFixes.size()) {
      throw new AssertionError(
          String.format(
              "%d quick fixes expected, found %d",
              expectedQuickFixes.size(), actualQuickFixes.size()));
    }

    List<QuickFix> unmatchedActuals = new ArrayList<>(actualQuickFixes);

    for (QuickFixExpectation expected : expectedQuickFixes) {

      Optional<QuickFix> matchingQuickFix =
          unmatchedActuals.stream()
              .filter(actual -> textEditsMatch(actual.getEdits(), expected.getExpectedTextEdits()))
              .findFirst();

      matchingQuickFix.ifPresent(unmatchedActuals::remove);
      if (matchingQuickFix.isPresent()) {
        unmatchedActuals.remove(matchingQuickFix.get());
      } else {
        throw new AssertionError(
            String.format(
                "Expected:%n%s%nFound %d non-matching quick fixes:%n%s",
                getQuickFixString(expected),
                actualQuickFixes.size(),
                actualQuickFixes.stream()
                    .map(this::getQuickFixString)
                    .collect(Collectors.joining("\n"))));
      }
    }

    if (!unmatchedActuals.isEmpty()) {
      throw new AssertionError(
          String.format(
              "Found %d unexpected quick fixes:%n%s",
              unmatchedActuals.size(),
              unmatchedActuals.stream()
                  .map(this::getQuickFixString)
                  .collect(Collectors.joining("\n"))));
    }
  }

  private String getQuickFixString(QuickFix quickFix) {
    Supplier<DelphiFileStream> fileStreamSupplier = newFileStreamSupplier(testFile.delphiFile());

    return "Quick fix:\n  "
        + quickFix.getEdits().stream()
            .map(QuickFixEditImpl.class::cast)
            .map(e -> e.toTextEdits(fileStreamSupplier))
            .flatMap(Collection::stream)
            .map(CheckVerifierImpl::getTextEditString)
            .collect(Collectors.joining("\n  "));
  }

  private static String getQuickFixString(QuickFixExpectation quickFix) {
    return "Quick fix "
        + quickFix.getFixId()
        + ":\n  "
        + quickFix.getExpectedTextEdits().stream()
            .map(CheckVerifierImpl::getTextEditString)
            .collect(Collectors.joining("\n  "));
  }

  private static String getTextEditString(TextRangeReplacement textEdit) {
    return String.format(
        "[%s:%s to %s:%s] replaced with %s",
        textEdit.getLocation().getBeginLine(),
        textEdit.getLocation().getBeginColumn(),
        textEdit.getLocation().getEndLine(),
        textEdit.getLocation().getEndColumn(),
        textEdit.getReplacement());
  }

  private static String getTextEditString(TextEditExpectation textEdit) {
    return String.format(
        "[%s:%s to %s:%s] replaced with %s",
        textEdit.getBeginLine(),
        textEdit.getBeginColumn(),
        textEdit.getEndLine(),
        textEdit.getEndColumn(),
        textEdit.getReplacement());
  }

  private static Supplier<DelphiFileStream> newFileStreamSupplier(DelphiFile delphiFile) {
    return Suppliers.memoize(
        () -> {
          try {
            return new DelphiFileStream(
                delphiFile.getSourceCodeFile().getAbsolutePath(),
                delphiFile.getSourceCodeFileEncoding());
          } catch (IOException e) {
            throw new UncheckedIOException(e);
          }
        });
  }

  private boolean textEditsMatch(
      List<QuickFixEdit> textEdits, List<TextEditExpectation> expectedTextEdits) {
    Supplier<DelphiFileStream> fileStreamSupplier = newFileStreamSupplier(testFile.delphiFile());

    List<TextRangeReplacement> unmatchedActuals =
        textEdits.stream()
            .map(QuickFixEditImpl.class::cast)
            .map(e -> e.toTextEdits(fileStreamSupplier))
            .flatMap(Collection::stream)
            .collect(Collectors.toCollection(ArrayList::new));

    if (expectedTextEdits.size() != unmatchedActuals.size()) {
      return false;
    }

    for (TextEditExpectation expected : expectedTextEdits) {
      Optional<TextRangeReplacement> matchingTextEdit =
          unmatchedActuals.stream().filter(actual -> textEditMatches(actual, expected)).findFirst();

      if (matchingTextEdit.isPresent()) {
        unmatchedActuals.remove(matchingTextEdit.get());
      } else {
        return false;
      }
    }

    return true;
  }

  private static boolean textEditMatches(
      TextRangeReplacement actual, TextEditExpectation expected) {
    boolean rangeMatches =
        actual.getLocation().getBeginLine() == expected.getBeginLine()
            && actual.getLocation().getBeginColumn() == expected.getBeginColumn()
            && actual.getLocation().getEndLine() == expected.getEndLine()
            && actual.getLocation().getEndColumn() == expected.getEndColumn();

    return rangeMatches && actual.getReplacement().equals(expected.getReplacement());
  }

  private static void assertIssueMismatchesEmpty(
      List<IssueExpectation> expectedIssues, List<IssueExpectation> unexpectedIssues) {
    if (!expectedIssues.isEmpty() || !unexpectedIssues.isEmpty()) {
      StringBuilder message = new StringBuilder("Issues were ");
      if (!expectedIssues.isEmpty()) {
        message.append("expected at ").append(expectedIssues);
      }
      if (!expectedIssues.isEmpty() && !unexpectedIssues.isEmpty()) {
        message.append(", ");
      }
      if (!unexpectedIssues.isEmpty()) {
        message.append("unexpected at ").append(unexpectedIssues);
      }
      throw new AssertionError(message.toString());
    }
  }

  @Override
  public void verifyIssueOnFile() {
    ExecutionResult result = execute();
    IssueLocation issueLocation = verifySingleIssueOnComponent(result.getIssues(), "file");

    if (!issueLocation.inputComponent().isFile()) {
      throw new AssertionError(
          "Expected the issue to be raised at file level, not at project level");
    }
  }

  @Override
  public void verifyIssueOnProject() {
    ExecutionResult result = execute();
    IssueLocation issueLocation = verifySingleIssueOnComponent(result.getIssues(), "project");

    if (issueLocation.inputComponent().isFile()) {
      throw new AssertionError(
          "Expected the issue to be raised at project level, not at file level");
    }
  }

  @Override
  public void verifyNoIssues() {
    ExecutionResult result = execute();
    List<Issue> issues = result.getIssues();

    if (!issues.isEmpty()) {
      throw new AssertionError(
          String.format(
              "No issues expected but got %d issues:%s", issues.size(), issuesToString(issues)));
    }
  }

  private ExecutionResult execute() {
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
            .preprocessorFactory(new DelphiPreprocessorFactory(compilerVersion, Platform.WINDOWS))
            .typeFactory(new TypeFactoryImpl(toolchain, compilerVersion))
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

    var compilerDirectiveParser =
        new CompilerDirectiveParserImpl(
            Platform.WINDOWS, file.getTextBlockLineEndingModeRegistry());

    var checkRegistrar = mock(MasterCheckRegistrar.class);
    when(checkRegistrar.getRuleKey(check))
        .thenReturn(Optional.of(RuleKey.of("test", check.getClass().getSimpleName())));
    when(checkRegistrar.getScope(check)).thenReturn(RuleScope.ALL);

    var context =
        new DelphiCheckContextTester(
            check, sensorContext, file, compilerDirectiveParser, checkRegistrar);

    check.start(context);
    check.visit(file.getAst(), context);
    check.end(context);

    return new ExecutionResult(List.copyOf(sensorContext.allIssues()), context.getQuickFixes());
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
              + "const\n"
              + "  vtInteger       = 0;\n"
              + "  vtBoolean       = 1;\n"
              + "  vtChar          = 2;\n"
              + "  vtExtended      = 3;\n"
              + "  vtString        = 4;\n"
              + "  vtPointer       = 5;\n"
              + "  vtPChar         = 6;\n"
              + "  vtObject        = 7;\n"
              + "  vtClass         = 8;\n"
              + "  vtWideChar      = 9;\n"
              + "  vtPWideChar     = 10;\n"
              + "  vtAnsiString    = 11;\n"
              + "  vtCurrency      = 12;\n"
              + "  vtVariant       = 13;\n"
              + "  vtInterface     = 14;\n"
              + "  vtWideString    = 15;\n"
              + "  vtInt64         = 16;\n"
              + "  vtUnicodeString = 17;\n"
              + "\n"
              + "type\n"
              + "  Int8    = ShortInt;\n"
              + "  Int16   = SmallInt;\n"
              + "  Int32   = Integer;\n"
              + "  IntPtr  = NativeInt;\n"
              + "  UInt8   = Byte;\n"
              + "  UInt16  = Word;\n"
              + "  UInt32  = Cardinal;\n"
              + "  UIntPtr = NativeUInt;\n"
              + "  Float32 = Single;\n"
              + "  Float64 = Double;\n"
              + "\n"
              + "  HRESULT = type Int32;\n"
              + "\n"
              + "  TArray<T> = array of T;\n"
              + "\n"
              + "  PGUID = ^TGUID;\n"
              + "  TGUID = record\n"
              + "  end;\n"
              + "\n"
              + "  PInterfaceEntry = ^TInterfaceEntry;\n"
              + "  TInterfaceEntry = packed record\n"
              + "  end;\n"
              + "\n"
              + "  PInterfaceTable = ^TInterfaceTable;\n"
              + "  TInterfaceTable = packed record\n"
              + "  end;\n"
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
              + "    class function MethodAddress(const Name: ShortString): Pointer; overload;\n"
              + "    class function MethodAddress(const Name: string): Pointer; overload;\n"
              + "    class function MethodName(Address: Pointer): string;\n"
              + "    class function QualifiedClassName: string;\n"
              + "    function FieldAddress(const Name: ShortString): Pointer; overload;\n"
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
              + "  IUnknown = IInterface;\n"
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
              + "  PLongInt       = ^LongInt;\n"
              + "  PInteger       = ^Integer;\n"
              + "  PCardinal      = ^Cardinal;\n"
              + "  PWord          = ^Word;\n"
              + "  PSmallInt      = ^SmallInt;\n"
              + "  {$POINTERMATH ON}\n"
              + "  PByte          = ^Byte;\n"
              + "  {$POINTERMATH OFF}\n"
              + "  PShortInt      = ^ShortInt;\n"
              + "  PUint32        = ^Uint32;\n"
              + "  PInt64         = ^Int64;\n"
              + "  PUInt64        = ^UInt64;\n"
              + "  PLongWord      = ^LongWord;\n"
              + "  PSingle        = ^Single;\n"
              + "  PDouble        = ^Double;\n"
              + "  PDate          = ^Double;\n"
              + "  PWordBool      = ^WordBool;\n"
              + "  PUnknown       = ^IUnknown;\n"
              + "  PPUnknown      = ^PUnknown;\n"
              + "  PInterface     = ^IInterface;\n"
              + "  PPWideChar     = ^PWideChar;\n"
              + "  PPChar         = PPWideChar;\n"
              + "  PExtended      = ^Extended;\n"
              + "  PComp          = ^Comp;\n"
              + "  PCurrency      = ^Currency;\n"
              + "  PVariant       = ^Variant;\n"
              + "  POleVariant    = ^OleVariant;\n"
              + "  PPointer       = ^Pointer;\n"
              + "  PBoolean       = ^Boolean;\n"
              + "  PNativeInt     = ^NativeInt;\n"
              + "  PNativeUInt    = ^NativeUInt;\n"
              + "  PShortString   = ^ShortString;\n"
              + "  PAnsiString    = ^AnsiString;\n"
              + "  PWideString    = ^WideString;\n"
              + "  PUnicodeString = ^UnicodeString;\n"
              + "  PString = PUnicodeString;\n"
              + "\n"
              + "  PVarRec = ^TVarRec;\n"
              + "  TVarRec = record\n"
              + "    case Integer of\n"
              + "      0: (case Byte of\n"
              + "            vtInteger:       (VInteger: Integer);\n"
              + "            vtBoolean:       (VBoolean: Boolean);\n"
              + "            vtChar:          (VChar: AnsiChar);\n"
              + "            vtExtended:      (VExtended: PExtended);\n"
              + "            vtString:        (VString: PShortString);\n"
              + "            vtPointer:       (VPointer: Pointer);\n"
              + "            vtPChar:         (VPChar: PAnsiChar);\n"
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
              + "  TVarArgList = record\n"
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

  private static class ExecutionResult {
    private final List<Issue> issues;
    private final List<QuickFix> quickFixes;

    public ExecutionResult(List<Issue> issues, List<QuickFix> quickFixes) {
      this.issues = issues;
      this.quickFixes = quickFixes;
    }

    public List<Issue> getIssues() {
      return issues;
    }

    public List<QuickFix> getQuickFixes() {
      return quickFixes;
    }
  }
}
