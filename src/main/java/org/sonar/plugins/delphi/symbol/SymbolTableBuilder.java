package org.sonar.plugins.delphi.symbol;

import static com.google.common.collect.Iterables.getFirst;
import static org.apache.commons.io.FilenameUtils.getBaseName;
import static org.sonar.plugins.delphi.utils.DelphiUtils.stopProgressReport;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
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
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableImplementationVisitor;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableInterfaceVisitor;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.Data;
import org.sonar.plugins.delphi.file.DelphiFile;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiFileConstructionException;
import org.sonar.plugins.delphi.file.DelphiFileConfig;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.SystemScope;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonarsource.analyzer.commons.ProgressReport;

public class SymbolTableBuilder {
  private static final Logger LOG = Loggers.get(SymbolTableBuilder.class);

  private static final String SYSTEM_UNIT_NOT_FOUND =
      "System unit could not be found. (Is '"
          + DelphiPlugin.STANDARD_LIBRARY_KEY
          + "' set correctly?))";

  private static final String REQUIRED_DEF_NOT_FOUND =
      "Required definition '%s' was not found in System.pas.";

  private static final String INVALID_STANDARD_LIBRARY_PATH =
      "Path to Delphi standard library is invalid: %s";

  private final SymbolTableVisitor interfaceVisitor = new SymbolTableInterfaceVisitor();
  private final SymbolTableVisitor implementationVisitor = new SymbolTableImplementationVisitor();
  private final SymbolTable symbolTable = new SymbolTable();
  private final Set<UnitData> sourceFileUnits = new HashSet<>();
  private final Multimap<String, UnitData> allUnitsByName = HashMultimap.create();
  private final Set<Path> unitPaths = new HashSet<>();

  private String encoding;
  private List<Path> searchDirectories = Collections.emptyList();
  private Set<String> conditionalDefines = Collections.emptySet();
  private Set<String> unitScopeNames = Collections.emptySet();
  private Map<String, String> unitAliases = Collections.emptyMap();

  private DelphiFileConfig fileConfig;
  private SystemScope systemScope;
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

  public SymbolTableBuilder searchDirectories(@NotNull List<Path> searchDirectories) {
    this.searchDirectories = searchDirectories;
    searchDirectories.forEach(this::processSearchPath);
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
      String message = String.format(INVALID_STANDARD_LIBRARY_PATH, absolutePath);
      throw new SymbolTableConstructionException(message);
    }
    this.processSearchPath(standardLibraryPath);
    return this;
  }

  private void processSearchPath(Path path) {
    findDelphiFilesRecursively(path).forEach(file -> createUnitData(file, false));
  }

  private static List<Path> findDelphiFilesRecursively(Path path) {
    try (Stream<Path> fileStream =
        Files.find(path, Integer.MAX_VALUE, (filePath, attributes) -> attributes.isRegularFile())) {
      return fileStream.filter(DelphiUtils::acceptFile).collect(Collectors.toList());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void createUnitData(Path unitPath, boolean isSourceFile) {
    if (!unitPaths.contains(unitPath)) {
      String unitName = getBaseName(unitPath.toString());
      UnitData unitData = new UnitData(unitPath);

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
    return findUnitByName(importName);
  }

  @Nullable
  private UnitData findUnitByName(String importName) {
    return getFirst(allUnitsByName.get(importName.toLowerCase()), null);
  }

  private void process(UnitData unit, ResolutionLevel resolutionLevel) {
    if (unit.resolved.ordinal() >= resolutionLevel.ordinal()) {
      return;
    }

    try {
      LOG.debug(StringUtils.repeat('\t', ++nestingLevel) + "> " + unit.unitFile.getFileName());

      fileConfig.setShouldSkipImplementation(resolutionLevel != ResolutionLevel.COMPLETE);

      DelphiFile delphiFile = DelphiFile.from(unit.unitFile.toFile(), fileConfig);

      if (unit.resolved == ResolutionLevel.NONE) {
        runSymbolTableVisitor(unit, delphiFile, ResolutionLevel.INTERFACE, interfaceVisitor);
      }

      if (resolutionLevel == ResolutionLevel.COMPLETE) {
        runSymbolTableVisitor(unit, delphiFile, ResolutionLevel.COMPLETE, implementationVisitor);
      }
    } catch (DelphiFileConstructionException e) {
      String error = String.format("Error while processing %s", unit.unitFile.toAbsolutePath());
      LOG.error(error, e);
    } finally {
      --nestingLevel;
    }
  }

  private void runSymbolTableVisitor(
      UnitData unit,
      DelphiFile delphiFile,
      ResolutionLevel resolutionLevel,
      SymbolTableVisitor visitor) {
    Data data =
        new Data(
            this::createImportDeclaration,
            delphiFile.getCompilerSwitchRegistry(),
            this.systemScope,
            unit.unitDeclaration);

    visitor.visit(delphiFile.getAst(), data);

    if (data.getUnitDeclaration() != null) {
      String filePath = unit.unitFile.toAbsolutePath().toString();
      unit.unitDeclaration = data.getUnitDeclaration();
      symbolTable.addUnit(filePath, unit.unitDeclaration);
    }

    unit.resolved = resolutionLevel;
  }

  private void indexUnit(UnitData unit, ResolutionLevel resolutionLevel) {
    LOG.debug("Indexing file: {}", unit.unitFile.toAbsolutePath());
    process(unit, resolutionLevel);
  }

  @NotNull
  private UnitData getSystemUnit() {
    UnitData systemData = getFirst(allUnitsByName.get("system"), null);
    if (systemData != null) {
      return systemData;
    }
    throw new SymbolTableConstructionException(SYSTEM_UNIT_NOT_FOUND);
  }

  private void indexSystemUnit() {
    UnitData systemData = getSystemUnit();
    indexUnit(systemData, ResolutionLevel.INTERFACE);
    this.systemScope = (SystemScope) systemData.unitDeclaration.getUnitScope();
    validateSystemScope();
  }

  private void validateSystemScope() {
    checkSystemDeclaration(systemScope.getTObjectDeclaration(), "TObject");
    checkSystemDeclaration(systemScope.getIInterfaceDeclaration(), "IInterface");
    checkSystemDeclaration(systemScope.getTVarRecDeclaration(), "TVarRec");
    checkSystemDeclaration(systemScope.getTClassHelperBaseDeclaration(), "TClassHelperBase");
  }

  private static void checkSystemDeclaration(NameDeclaration declaration, String name) {
    if (declaration == null) {
      throw new SymbolTableConstructionException(String.format(REQUIRED_DEF_NOT_FOUND, name));
    }
  }

  public SymbolTable build() {
    fileConfig = DelphiFile.createConfig(encoding, searchDirectories, conditionalDefines);
    ProgressReport progressReport =
        new ProgressReport(
            "Report about progress of Symbol Table construction",
            TimeUnit.SECONDS.toMillis(10),
            "indexed");

    progressReport.start(
        new ImmutableList.Builder<String>()
            .add(getSystemUnit().unitFile.toString())
            .addAll(getSourceFileNames())
            .build());

    indexSystemUnit();
    progressReport.nextFile();

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
        .collect(Collectors.toList());
  }

  private enum ResolutionLevel {
    NONE,
    INTERFACE,
    COMPLETE
  }

  private static class UnitData {
    private final Path unitFile;
    private ResolutionLevel resolved;
    private UnitNameDeclaration unitDeclaration;

    private UnitData(Path unitFile) {
      this.unitFile = unitFile;
      this.resolved = ResolutionLevel.NONE;
    }
  }

  private static class SymbolTableConstructionException extends RuntimeException {
    SymbolTableConstructionException(String message) {
      super(message);
    }
  }
}
