/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.symbol;

import static au.com.integradev.delphi.utils.DelphiUtils.stopProgressReport;

import au.com.integradev.delphi.DelphiProperties;
import au.com.integradev.delphi.antlr.ast.visitors.DependencyAnalysisVisitor;
import au.com.integradev.delphi.antlr.ast.visitors.SymbolTableVisitor;
import au.com.integradev.delphi.file.DelphiFile;
import au.com.integradev.delphi.file.DelphiFile.DelphiFileConstructionException;
import au.com.integradev.delphi.file.DelphiFileConfig;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.symbol.declaration.UnitImportNameDeclarationImpl;
import au.com.integradev.delphi.utils.DelphiUtils;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import org.sonar.plugins.communitydelphi.api.symbol.Qualifiable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.SysInitScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.SystemScope;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;
import org.sonarsource.analyzer.commons.ProgressReport;

public class SymbolTableBuilder {
  private static final Logger LOG = LoggerFactory.getLogger(SymbolTableBuilder.class);

  private final SymbolTable symbolTable = new SymbolTable();
  private final Set<UnitData> sourceFileUnits = new HashSet<>();
  private final HashMap<String, UnitData> allUnitsByName = new HashMap<>();
  private final Set<Path> unitPaths = new HashSet<>();
  private String encoding;
  private DelphiPreprocessorFactory preprocessorFactory;
  private TypeFactory typeFactory;
  private Path standardLibraryPath;
  private SearchPath searchPath = SearchPath.create(Collections.emptyList());
  private List<Path> sourceFiles = Collections.emptyList();
  private List<Path> referencedFiles = Collections.emptyList();
  private Set<String> conditionalDefines = Collections.emptySet();
  private Set<String> unitScopeNames = Collections.emptySet();
  private Map<String, String> unitAliases = Collections.emptyMap();

  private SystemScope systemScope;
  private SysInitScope sysInitScope;
  private int nestingLevel;

  SymbolTableBuilder() {
    // package-private constructor
  }

  public SymbolTableBuilder unitScopeNames(Set<String> unitScopeNames) {
    this.unitScopeNames = unitScopeNames;
    return this;
  }

  public SymbolTableBuilder sourceFiles(List<Path> sourceFiles) {
    this.sourceFiles = sourceFiles;
    return this;
  }

  public SymbolTableBuilder referencedFiles(List<Path> referencedFiles) {
    this.referencedFiles = referencedFiles;
    return this;
  }

  public SymbolTableBuilder encoding(String encoding) {
    this.encoding = encoding;
    return this;
  }

  public SymbolTableBuilder preprocessorFactory(DelphiPreprocessorFactory preprocessorFactory) {
    this.preprocessorFactory = preprocessorFactory;
    return this;
  }

  public SymbolTableBuilder typeFactory(TypeFactory typeFactory) {
    this.typeFactory = typeFactory;
    return this;
  }

  public SymbolTableBuilder searchPath(SearchPath searchPath) {
    this.searchPath = searchPath;
    return this;
  }

  public SymbolTableBuilder conditionalDefines(Set<String> conditionalDefines) {
    this.conditionalDefines = conditionalDefines;
    return this;
  }

  public SymbolTableBuilder unitAliases(Map<String, String> unitAliases) {
    this.unitAliases = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    this.unitAliases.putAll(unitAliases);
    return this;
  }

  public SymbolTableBuilder standardLibraryPath(Path standardLibraryPath) {
    this.standardLibraryPath = standardLibraryPath;
    return this;
  }

  private void processStandardLibrarySearchPaths() {
    if (standardLibraryPath == null) {
      return;
    }

    if (!Files.exists(standardLibraryPath)) {
      Path absolutePath = standardLibraryPath.toAbsolutePath();
      throw new SymbolTableConstructionException(
          String.format("Path to Delphi standard library is invalid: %s", absolutePath));
    }

    Path tools = standardLibraryPath.resolve("Tools");

    try (Stream<Path> fileStream =
        Files.find(
            standardLibraryPath,
            Integer.MAX_VALUE,
            (filePath, attributes) -> attributes.isRegularFile())) {
      fileStream
          .filter(DelphiUtils::acceptFile)
          .filter(path -> !path.startsWith(tools))
          .forEach(file -> createUnitData(file, false));
    } catch (IOException e) {
      throw new SymbolTableConstructionException(e);
    }
  }

  private void processSearchPath(Path path) {
    try (Stream<Path> fileStream = Files.list(path)) {
      fileStream
          .filter(Files::isRegularFile)
          .filter(DelphiUtils::acceptFile)
          .forEach(file -> createUnitData(file, false));
    } catch (IOException e) {
      throw new SymbolTableConstructionException(e);
    }
  }

  private void createUnitData(Path unitPath, boolean isSourceFile) {
    if (unitPaths.add(unitPath) || isSourceFile) {
      String unitName = FilenameUtils.getBaseName(unitPath.toString());
      UnitData unitData = new UnitData(unitPath, isSourceFile);

      if (isSourceFile) {
        sourceFileUnits.add(unitData);
      }

      UnitData existing = allUnitsByName.get(unitName.toLowerCase());
      if (existing == null || existing.unitFile.equals(unitPath)) {
        allUnitsByName.put(unitName.toLowerCase(), unitData);
      }
    }
  }

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

    return new UnitImportNameDeclarationImpl(node, unitDeclaration);
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
    return allUnitsByName.get(importName.toLowerCase());
  }

  private DelphiFileConfig createFileConfig(UnitData unit, boolean shouldSkipImplementation) {
    return DelphiFile.createConfig(
        sourceFileUnits.contains(unit) ? encoding : null,
        preprocessorFactory,
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

  private UnitData getRequiredUnit(String unit) {
    UnitData data = allUnitsByName.get(unit.toLowerCase());
    if (data != null) {
      return data;
    }
    throw new SymbolTableConstructionException(
        String.format(
            "%s unit could not be found. (Is '"
                + DelphiProperties.BDS_PATH_KEY
                + "' set correctly?)",
            unit));
  }

  private UnitData getSystemUnit() {
    return getRequiredUnit("System");
  }

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
    if (preprocessorFactory == null) {
      throw new SymbolTableConstructionException("preprocessorFactory was not supplied.");
    }

    if (typeFactory == null) {
      throw new SymbolTableConstructionException("typeFactory was not supplied.");
    }

    processStandardLibrarySearchPaths();
    searchPath.getRootDirectories().forEach(this::processSearchPath);
    referencedFiles.forEach(file -> this.createUnitData(file, false));
    sourceFiles.forEach(file -> this.createUnitData(file, true));

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
