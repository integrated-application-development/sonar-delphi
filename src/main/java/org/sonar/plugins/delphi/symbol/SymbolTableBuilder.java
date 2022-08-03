package org.sonar.plugins.delphi.symbol;

import static com.google.common.collect.Iterables.getFirst;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.sonar.plugins.delphi.utils.DelphiUtils.commonPath;
import static org.sonar.plugins.delphi.utils.DelphiUtils.stopProgressReport;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.SetMultimap;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.DependencyAnalysisVisitor;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor;
import org.sonar.plugins.delphi.file.DelphiFile;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiFileConstructionException;
import org.sonar.plugins.delphi.file.DelphiFileConfig;
import org.sonar.plugins.delphi.preprocessor.search.SearchPath;
import org.sonar.plugins.delphi.symbol.declaration.MethodDirective;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.FileScope;
import org.sonar.plugins.delphi.symbol.scope.SysInitScope;
import org.sonar.plugins.delphi.symbol.scope.SystemScope;
import org.sonar.plugins.delphi.type.Type.ScopedType;
import org.sonar.plugins.delphi.type.factory.TypeFactory;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonarsource.analyzer.commons.ProgressReport;

public class SymbolTableBuilder {
  private static final Logger LOG = Loggers.get(SymbolTableBuilder.class);

  private final SymbolTable symbolTable = new SymbolTable();
  private final Set<UnitData> sourceFileUnits = new HashSet<>();
  private final SetMultimap<String, UnitData> allUnitsByName = HashMultimap.create();
  private final Set<Path> unitPaths = new HashSet<>();

  private String encoding;
  private TypeFactory typeFactory;
  private SearchPath searchPath = SearchPath.create(Collections.emptyList());
  private Set<String> conditionalDefines = Collections.emptySet();
  private Set<String> unitScopeNames = Collections.emptySet();
  private Map<String, String> unitAliases = Collections.emptyMap();

  private SystemScope systemScope;
  private SysInitScope sysInitScope;
  private int nestingLevel;

  SymbolTableBuilder() {
    // package-private constructor
  }

  public SymbolTableBuilder unitScopeNames(@NotNull Set<String> unitScopeNames) {
    this.unitScopeNames = unitScopeNames;
    return this;
  }

  public SymbolTableBuilder sourceFiles(@NotNull Iterable<Path> sourceFiles) {
    sourceFiles.forEach(file -> this.createUnitData(file, true));
    return this;
  }

  public SymbolTableBuilder encoding(String encoding) {
    this.encoding = encoding;
    return this;
  }

  public SymbolTableBuilder typeFactory(TypeFactory typeFactory) {
    this.typeFactory = typeFactory;
    return this;
  }

  public SymbolTableBuilder searchPath(@NotNull SearchPath searchPath) {
    this.searchPath = searchPath;
    this.searchPath.getRootDirectories().forEach(this::processSearchPath);
    return this;
  }

  public SymbolTableBuilder conditionalDefines(@NotNull Set<String> conditionalDefines) {
    this.conditionalDefines = conditionalDefines;
    return this;
  }

  public SymbolTableBuilder unitAliases(@NotNull Map<String, String> unitAliases) {
    this.unitAliases = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    this.unitAliases.putAll(unitAliases);
    return this;
  }

  public SymbolTableBuilder standardLibraryPath(@NotNull Path standardLibraryPath) {
    if (!Files.exists(standardLibraryPath)) {
      Path absolutePath = standardLibraryPath.toAbsolutePath();
      throw new SymbolTableConstructionException(
          String.format("Path to Delphi standard library is invalid: %s", absolutePath));
    }
    Path tools = standardLibraryPath.resolve("Tools");
    this.processSearchPath(standardLibraryPath, (Path path) -> !path.startsWith(tools));
    return this;
  }

  private void processSearchPath(Path path) {
    processSearchPath(path, (Path p) -> true);
  }

  private void processSearchPath(Path path, Predicate<Path> predicate) {
    findDelphiFilesRecursively(path).stream()
        .filter(predicate)
        .forEach(file -> createUnitData(file, false));
  }

  private static List<Path> findDelphiFilesRecursively(Path path) {
    try (Stream<Path> fileStream =
        Files.find(path, Integer.MAX_VALUE, (filePath, attributes) -> attributes.isRegularFile())) {
      return fileStream.filter(DelphiUtils::acceptFile).collect(Collectors.toList());
    } catch (IOException e) {
      throw new SymbolTableConstructionException(e);
    }
  }

  private void createUnitData(Path unitPath, boolean isSourceFile) {
    if (!unitPaths.contains(unitPath)) {
      String unitName = getBaseName(unitPath.toString());
      UnitData unitData = new UnitData(unitPath, isSourceFile);

      if (isSourceFile) {
        sourceFileUnits.add(unitData);
      }

      allUnitsByName.put(unitName.toLowerCase(), unitData);
      unitPaths.add(unitPath);
    }
  }

  @NotNull
  private UnitImportNameDeclaration createImportDeclaration(
      UnitNameDeclaration unit, UnitImportNode node) {
    UnitData data = searchForImport(unit, node.getNameNode());

    UnitNameDeclaration unitDeclaration = null;
    if (data != null) {
      process(data, ResolutionLevel.INTERFACE);
      unitDeclaration = data.unitDeclaration;
    } else {
      LOG.debug(
          StringUtils.repeat('\t', nestingLevel + 1)
              + "X "
              + node.getNameNode().fullyQualifiedName()
              + " **Failed to locate unit**");
    }

    return new UnitImportNameDeclaration(node, unitDeclaration);
  }

  @Nullable
  private UnitData searchForImport(UnitNameDeclaration unit, Qualifiable qualifiableImportName) {
    String importName = qualifiableImportName.fullyQualifiedName();
    String aliased = unitAliases.get(importName);

    if (aliased != null) {
      importName = aliased;
    }

    UnitData data = findImportByName(unit, importName);

    if (data == null) {
      for (String unitScopeName : unitScopeNames) {
        data = findImportByName(unit, unitScopeName + "." + importName);
        if (data != null) {
          break;
        }
      }
    }

    if (data == null) {
      String namespace = unit.getNamespace();
      if (!qualifiableImportName.isQualified() && !namespace.isEmpty()) {
        data = findImportByName(unit, namespace + "." + importName);
      }
    }

    return data;
  }

  @Nullable
  private UnitData findImportByName(UnitNameDeclaration unit, String importName) {
    if (unit.getImage().equalsIgnoreCase(importName)) {
      return null;
    }

    return allUnitsByName.get(importName.toLowerCase()).stream()
        .max(
            (o1, o2) ->
                ComparisonChain.start()
                    .compare(
                        commonPathNameCount(unit.getPath(), o1.unitFile),
                        commonPathNameCount(unit.getPath(), o2.unitFile))
                    .compare(o2.unitFile.getNameCount(), o1.unitFile.getNameCount())
                    .compare(o1.unitFile, o2.unitFile)
                    .result())
        .orElse(null);
  }

  private static int commonPathNameCount(Path pathA, Path pathB) {
    Path commonPath = commonPath(pathA, pathB);
    if (commonPath != null) {
      return commonPath.getNameCount();
    }
    return -1;
  }

  private DelphiFileConfig createFileConfig(UnitData unit, boolean shouldSkipImplementation) {
    return DelphiFile.createConfig(
        sourceFileUnits.contains(unit) ? encoding : null,
        typeFactory,
        searchPath,
        conditionalDefines,
        shouldSkipImplementation);
  }

  private void process(UnitData unit, ResolutionLevel resolutionLevel) {
    if (unit.resolved.ordinal() >= resolutionLevel.ordinal()) {
      return;
    }

    try {
      ++nestingLevel;
      LOG.debug(StringUtils.repeat('\t', nestingLevel) + "> " + unit.unitFile.getFileName());

      boolean shouldSkipImplementation = (resolutionLevel != ResolutionLevel.COMPLETE);
      DelphiFileConfig fileConfig = createFileConfig(unit, shouldSkipImplementation);
      DelphiFile delphiFile = DelphiFile.from(unit.unitFile.toFile(), fileConfig);

      if (unit.resolved == ResolutionLevel.NONE) {
        runSymbolTableVisitor(unit, delphiFile, ResolutionLevel.INTERFACE);
        runDependencyAnalysisVisitor(unit, delphiFile, ResolutionLevel.INTERFACE);
      }

      if (resolutionLevel == ResolutionLevel.COMPLETE) {
        runSymbolTableVisitor(unit, delphiFile, ResolutionLevel.COMPLETE);
        processImportsWithInlineMethods(unit);
        runDependencyAnalysisVisitor(unit, delphiFile, ResolutionLevel.COMPLETE);
      }
    } catch (DelphiFileConstructionException e) {
      String error = String.format("Error while processing %s", unit.unitFile.toAbsolutePath());
      LOG.error(error, e);
    } finally {
      --nestingLevel;
    }
  }

  private void runSymbolTableVisitor(
      UnitData unit, DelphiFile delphiFile, ResolutionLevel resolutionLevel) {
    var data =
        new SymbolTableVisitor.Data(
            typeFactory,
            delphiFile.getCompilerSwitchRegistry(),
            this::createImportDeclaration,
            this.systemScope,
            this.sysInitScope,
            unit.unitDeclaration);

    symbolTableVisitor(resolutionLevel).visit(delphiFile.getAst(), data);

    if (data.getUnitDeclaration() != null) {
      String filePath = unit.unitFile.toAbsolutePath().toString();
      unit.unitDeclaration = data.getUnitDeclaration();
      symbolTable.addUnit(filePath, unit.unitDeclaration);
      if (!unit.isSourceFile) {
        FileScope fileScope = data.getUnitDeclaration().getFileScope();
        fileScope.unregisterScopes();
        fileScope.unregisterDeclarations();
        fileScope.unregisterOccurrences();
      }
    }

    unit.resolved = resolutionLevel;
  }

  private void runDependencyAnalysisVisitor(
      UnitData unit, DelphiFile delphiFile, ResolutionLevel resolutionLevel) {
    var data = new DependencyAnalysisVisitor.Data(unit.unitDeclaration);
    dependencyVisitor(resolutionLevel).visit(delphiFile.getAst(), data);
  }

  private static SymbolTableVisitor symbolTableVisitor(ResolutionLevel resolutionLevel) {
    if (resolutionLevel == ResolutionLevel.INTERFACE) {
      return SymbolTableVisitor.interfaceVisitor();
    } else {
      return SymbolTableVisitor.implementationVisitor();
    }
  }

  private static DependencyAnalysisVisitor dependencyVisitor(ResolutionLevel resolutionLevel) {
    if (resolutionLevel == ResolutionLevel.INTERFACE) {
      return DependencyAnalysisVisitor.interfaceVisitor();
    } else {
      return DependencyAnalysisVisitor.implementationVisitor();
    }
  }

  private void processImportsWithInlineMethods(UnitData unit) {
    Set<UnitData> imports =
        unit.unitDeclaration.getScope().getImportDeclarations().stream()
            .map(UnitImportNameDeclaration::getOriginalDeclaration)
            .filter(Objects::nonNull)
            .filter(SymbolTableBuilder::hasInlineMethods)
            .map(UnitNameDeclaration::getName)
            .map(name -> findImportByName(unit.unitDeclaration, name))
            .filter(Objects::nonNull)
            .filter(unitData -> unitData.resolved == ResolutionLevel.INTERFACE)
            .collect(Collectors.toSet());

    if (imports.isEmpty()) {
      return;
    }

    for (UnitData imported : imports) {
      process(imported, ResolutionLevel.COMPLETE);
    }
  }

  private static boolean hasInlineMethods(UnitNameDeclaration unit) {
    return hasInlineMethods(unit.getFileScope());
  }

  private static boolean hasInlineMethods(DelphiScope scope) {
    if (scope.getMethodDeclarations().stream()
        .anyMatch(method -> method.hasDirective(MethodDirective.INLINE))) {
      return true;
    }

    return scope.getTypeDeclarations().stream()
        .map(TypeNameDeclaration::getType)
        .filter(ScopedType.class::isInstance)
        .map(ScopedType.class::cast)
        .map(ScopedType::typeScope)
        .anyMatch(SymbolTableBuilder::hasInlineMethods);
  }

  private void indexUnit(UnitData unit, ResolutionLevel resolutionLevel) {
    LOG.debug("Indexing file: {}", unit.unitFile.toAbsolutePath());
    process(unit, resolutionLevel);
  }

  @NotNull
  private UnitData getRequiredUnit(String unit) {
    UnitData data = getFirst(allUnitsByName.get(unit.toLowerCase()), null);
    if (data != null) {
      return data;
    }
    throw new SymbolTableConstructionException(
        String.format(
            "%s unit could not be found. (Is '" + DelphiPlugin.BDS_PATH_KEY + "' set correctly?)",
            unit));
  }

  @NotNull
  private UnitData getSystemUnit() {
    return getRequiredUnit("System");
  }

  @NotNull
  private UnitData getSysInitUnit() {
    return getRequiredUnit("SysInit");
  }

  private void indexSystemUnit() {
    LOG.info("Indexing System unit...");
    UnitData systemData = getSystemUnit();
    indexUnit(systemData, ResolutionLevel.INTERFACE);
    this.systemScope = (SystemScope) systemData.unitDeclaration.getFileScope();
    validateSystemScope();
  }

  private void indexSysInitUnit() {
    LOG.info("Indexing SysInit unit...");
    UnitData sysInitData = getSysInitUnit();
    indexUnit(sysInitData, ResolutionLevel.INTERFACE);
    this.sysInitScope = (SysInitScope) sysInitData.unitDeclaration.getFileScope();
  }

  private void validateSystemScope() {
    checkSystemDeclaration(systemScope.getTObjectDeclaration(), "TObject");
    checkSystemDeclaration(systemScope.getIInterfaceDeclaration(), "IInterface");
    checkSystemDeclaration(systemScope.getTVarRecDeclaration(), "TVarRec");
    checkSystemDeclaration(systemScope.getTClassHelperBaseDeclaration(), "TClassHelperBase");
  }

  private static void checkSystemDeclaration(NameDeclaration declaration, String name) {
    if (declaration == null) {
      throw new SymbolTableConstructionException(
          String.format("Required definition '%s' was not found in System.pas.", name));
    }
  }

  public SymbolTable build() {
    if (typeFactory == null) {
      throw new SymbolTableConstructionException("TypeFactory was not supplied.");
    }

    ProgressReport progressReport =
        new ProgressReport(
            "Report about progress of Symbol Table construction",
            TimeUnit.SECONDS.toMillis(10),
            "indexed");

    indexSystemUnit();
    indexSysInitUnit();
    progressReport.start(getSourceFileNames());

    boolean success = false;

    try {
      for (UnitData unit : sourceFileUnits) {
        indexUnit(unit, ResolutionLevel.COMPLETE);
        progressReport.nextFile();
      }
      success = true;
    } finally {
      stopProgressReport(progressReport, success);
    }

    return symbolTable;
  }

  private Iterable<String> getSourceFileNames() {
    return sourceFileUnits.stream()
        .map(data -> data.unitFile)
        .map(Path::toString)
        .collect(Collectors.toUnmodifiableList());
  }

  private enum ResolutionLevel {
    NONE,
    INTERFACE,
    COMPLETE
  }

  private static class UnitData {
    private final Path unitFile;
    private final boolean isSourceFile;
    private ResolutionLevel resolved;
    private UnitNameDeclaration unitDeclaration;

    private UnitData(Path unitFile, boolean isSourceFile) {
      this.unitFile = unitFile;
      this.isSourceFile = isSourceFile;
      this.resolved = ResolutionLevel.NONE;
    }
  }

  public static class SymbolTableConstructionException extends RuntimeException {
    SymbolTableConstructionException(String message) {
      super(message);
    }

    SymbolTableConstructionException(Exception cause) {
      super(cause);
    }
  }
}
