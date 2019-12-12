package org.sonar.plugins.delphi.symbol;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getFirst;
import static java.lang.String.format;
import static java.util.Objects.isNull;
import static org.apache.commons.io.FilenameUtils.getBaseName;

import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.Data;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.ResolutionLevel;
import org.sonar.plugins.delphi.file.DelphiFile;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiFileConstructionException;
import org.sonar.plugins.delphi.file.DelphiFileConfig;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.SystemScope;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.delphi.utils.ProgressReporter;
import org.sonar.plugins.delphi.utils.ProgressReporterLogger;

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

  private final SymbolTableVisitor visitor = new SymbolTableVisitor();
  private final SymbolTable symbolTable = new SymbolTable();
  private final Set<UnitData> sourceFileUnits = new HashSet<>();
  private final Multimap<String, UnitData> allUnitsByName = HashMultimap.create();
  private final Set<String> unitPaths = new HashSet<>();

  private String projectName;
  private Set<String> unitScopeNames;
  private DelphiFileConfig fileConfig;

  private SystemScope systemScope;

  SymbolTableBuilder() {
    // package-private constructor
  }

  public SymbolTableBuilder project(@NotNull DelphiProject project) {
    this.projectName = project.getName();
    this.unitScopeNames = project.getUnitScopeNames();
    project.getSourceFiles().forEach(file -> this.createUnitData(file, true));
    project.getSearchPath().forEach(this::processSearchPath);
    return this;
  }

  public SymbolTableBuilder standardLibraryPath(@NotNull Path standardLibraryPath) {
    if (!Files.exists(standardLibraryPath)) {
      String message = format(INVALID_STANDARD_LIBRARY_PATH, standardLibraryPath.toAbsolutePath());
      throw new SymbolTableConstructionException(message);
    }
    this.processSearchPath(standardLibraryPath);
    return this;
  }

  public SymbolTableBuilder fileConfig(@NotNull DelphiFileConfig fileConfig) {
    Preconditions.checkState(isNull(this.fileConfig));
    this.fileConfig = fileConfig;
    return this;
  }

  private void processSearchPath(Path path) {
    findDelphiFilesRecursively(path).forEach(file -> createUnitData(file, false));
  }

  private static List<File> findDelphiFilesRecursively(Path path) {
    try (Stream<Path> fileStream =
        Files.find(path, Integer.MAX_VALUE, (filePath, attributes) -> attributes.isRegularFile())) {
      return fileStream
          .map(Path::toFile)
          .filter(DelphiUtils::acceptFile)
          .collect(Collectors.toList());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void createUnitData(File file, boolean isSourceFile) {
    if (!unitPaths.contains(file.getAbsolutePath())) {
      String unitName = getBaseName(file.getName());
      UnitData unitData = new UnitData(file);

      if (isSourceFile) {
        sourceFileUnits.add(unitData);
      }

      allUnitsByName.put(unitName.toLowerCase(), unitData);
      unitPaths.add(file.getAbsolutePath());
    }
  }

  @Nullable
  private UnitData findUnitByName(String unitName) {
    return getFirst(allUnitsByName.get(unitName.toLowerCase()), null);
  }

  @Nullable
  private UnitData searchForUnit(String namespace, Qualifiable importName) {
    String unitName = importName.fullyQualifiedName();
    UnitData data = null;

    if (!importName.isQualified() && !namespace.isEmpty()) {
      data = findUnitByName(namespace + "." + unitName);
    }

    if (data == null) {
      data = findUnitByName(unitName);
    }

    if (data == null) {
      for (String unitScopeName : unitScopeNames) {
        data = findUnitByName(unitScopeName + "." + unitName);
        if (data != null) {
          break;
        }
      }
    }

    return data;
  }

  @NotNull
  private UnitImportNameDeclaration createImportDeclaration(String namespace, UnitImportNode node) {
    UnitData data = searchForUnit(namespace, node.getNameNode());

    UnitNameDeclaration unitDeclaration = null;
    if (data != null) {
      process(data, ResolutionLevel.INTERFACE);
      unitDeclaration = data.unitDeclaration;
    } else {
      LOG.debug("Failed to resolve unit import: {}", node.getNameNode().fullyQualifiedName());
    }

    return new UnitImportNameDeclaration(node, unitDeclaration);
  }

  private void process(UnitData unit, ResolutionLevel resolutionLevel) {
    try {
      if (unit.resolved.ordinal() < resolutionLevel.ordinal()) {
        LOG.debug("\t>> " + unit.sourceFile.getName());
        DelphiFile delphiFile = DelphiFile.from(unit.sourceFile, fileConfig);
        Data data =
            new Data(
                unit.resolved,
                resolutionLevel,
                this::createImportDeclaration,
                this.systemScope,
                unit.unitDeclaration);

        visitor.visit(delphiFile.getAst(), data);

        if (data.getUnitDeclaration() != null) {
          String filePath = unit.sourceFile.getAbsolutePath();
          unit.unitDeclaration = data.getUnitDeclaration();
          symbolTable.addUnit(filePath, unit.unitDeclaration);
        }

        unit.resolved = resolutionLevel;
      }
    } catch (DelphiFileConstructionException e) {
      String error = format("Error while indexing %s", unit.sourceFile.getAbsolutePath());
      LOG.error(error, e);
    }
  }

  private void indexUnit(UnitData unit, ResolutionLevel resolutionLevel) {
    LOG.debug("Indexing file [{}]: {}", resolutionLevel, unit.sourceFile.getAbsolutePath());
    process(unit, resolutionLevel);
  }

  private void indexSystemUnit() {
    UnitData systemData = getFirst(allUnitsByName.get("system"), null);
    if (systemData != null) {
      indexUnit(systemData, ResolutionLevel.INTERFACE);
      if (systemData.unitDeclaration != null) {
        this.systemScope = (SystemScope) systemData.unitDeclaration.getUnitScope();
        validateSystemScope();
        return;
      }
    }
    throw new SymbolTableConstructionException(SYSTEM_UNIT_NOT_FOUND);
  }

  private void validateSystemScope() {
    if (systemScope.getTObjectDeclaration() == null) {
      throw new SymbolTableConstructionException(format(REQUIRED_DEF_NOT_FOUND, "TObject"));
    }
    if (systemScope.getIInterfaceDeclaration() == null) {
      throw new SymbolTableConstructionException(format(REQUIRED_DEF_NOT_FOUND, "IInterface"));
    }
  }

  public SymbolTable build() {
    checkNotNull(projectName, "project must be supplied to SymbolTableBuilder");
    checkNotNull(fileConfig, "fileConfig must be supplied to SymbolTableBuilder");

    LOG.info("Indexing project: {}", projectName);

    ProgressReporter progressReporter =
        new ProgressReporter((sourceFileUnits.size() * 2) + 1, 10, new ProgressReporterLogger(LOG));

    indexSystemUnit();
    progressReporter.progress();

    for (UnitData unit : sourceFileUnits) {
      indexUnit(unit, ResolutionLevel.INTERFACE);
      progressReporter.progress();
    }

    for (UnitData unit : sourceFileUnits) {
      indexUnit(unit, ResolutionLevel.COMPLETE);
      progressReporter.progress();
    }

    return symbolTable;
  }

  private static class UnitData {
    private final File sourceFile;
    private ResolutionLevel resolved;
    private UnitNameDeclaration unitDeclaration;

    private UnitData(File sourceFile) {
      this.sourceFile = sourceFile;
      this.resolved = ResolutionLevel.NONE;
    }
  }

  private static class SymbolTableConstructionException extends RuntimeException {
    SymbolTableConstructionException(String message) {
      super(message);
    }
  }
}
