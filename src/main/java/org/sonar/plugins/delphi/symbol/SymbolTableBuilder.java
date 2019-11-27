package org.sonar.plugins.delphi.symbol;

import static com.google.common.collect.Iterables.getFirst;
import static org.apache.commons.io.FilenameUtils.getBaseName;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.Data;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.ResolutionLevel;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStream;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStreamConfig;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.file.DelphiFile;
import org.sonar.plugins.delphi.file.DelphiFile.DelphiFileConstructionException;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.ProgressReporter;
import org.sonar.plugins.delphi.utils.ProgressReporterLogger;

public class SymbolTableBuilder {

  private static class UnitData {
    private final File sourceFile;
    private ResolutionLevel resolved;
    private UnitNameDeclaration unitDeclaration;

    private UnitData(File sourceFile) {
      this.sourceFile = sourceFile;
      this.resolved = ResolutionLevel.NONE;
    }
  }

  private static final Logger LOG = Loggers.get(SymbolTableBuilder.class);

  private final DelphiProject project;
  private final DelphiFileStreamConfig fileStreamConfig;
  private final SymbolTableVisitor visitor;
  private final SymbolTable symbolTable;
  private final Set<UnitData> unitDataSet;
  private final Multimap<String, UnitData> unitDataByName;

  SymbolTableBuilder(DelphiProject project, DelphiProjectHelper delphiProjectHelper) {
    this.project = project;
    this.fileStreamConfig = DelphiFileStream.createConfig(project, delphiProjectHelper);
    this.visitor = new SymbolTableVisitor();
    this.symbolTable = new SymbolTable();
    this.unitDataByName = HashMultimap.create();
    this.unitDataSet = new HashSet<>();

    this.createUnitData();
  }

  private void createUnitData() {
    for (File sourceFile : project.getSourceFiles()) {
      String unitName = getBaseName(sourceFile.getName());
      UnitData unitData = new UnitData(sourceFile);

      unitDataByName.put(unitName, unitData);
      unitDataSet.add(unitData);
    }
  }

  @NotNull
  private UnitImportNameDeclaration createImportDeclaration(UnitImportNode importNode) {
    String unitName = importNode.getNameNode().getImage();
    UnitData data = getFirst(unitDataByName.get(unitName), null);
    UnitNameDeclaration unitDeclaration = null;
    if (data != null) {
      process(data, ResolutionLevel.INTERFACE);
      unitDeclaration = data.unitDeclaration;
    }
    return new UnitImportNameDeclaration(importNode, unitDeclaration);
  }

  private void process(UnitData unitData, ResolutionLevel resolutionLevel) {
    if (unitData.resolved.ordinal() < resolutionLevel.ordinal()) {
      DelphiFile delphiFile = DelphiFile.from(unitData.sourceFile, fileStreamConfig);
      Data data =
          new Data(
              unitData.resolved,
              resolutionLevel,
              this::createImportDeclaration,
              unitData.unitDeclaration);

      visitor.visit(delphiFile.getAst(), data);

      if (data.getUnitDeclaration() != null) {
        String filePath = unitData.sourceFile.getAbsolutePath();
        unitData.unitDeclaration = data.getUnitDeclaration();
        symbolTable.addUnit(filePath, unitData.unitDeclaration);
      }

      unitData.resolved = resolutionLevel;
    }
  }

  private void indexUnit(UnitData unit, ResolutionLevel resolutionLevel) {
    try {
      LOG.debug("Indexing file [{}]: {}", resolutionLevel, unit.sourceFile.getAbsolutePath());
      process(unit, resolutionLevel);
    } catch (DelphiFileConstructionException e) {
      String error = String.format("Error while indexing %s", unit.sourceFile.getAbsolutePath());
      LOG.error(error, e);
    }
  }

  public SymbolTable build() {
    LOG.info("Indexing project: {}", project.getName());

    ProgressReporter progressReporter =
        new ProgressReporter(unitDataSet.size() * 2, 10, new ProgressReporterLogger(LOG));

    for (UnitData unit : unitDataSet) {
      indexUnit(unit, ResolutionLevel.INTERFACE);
      progressReporter.progress();
    }

    for (UnitData unit : unitDataSet) {
      indexUnit(unit, ResolutionLevel.COMPLETE);
      progressReporter.progress();
    }

    return symbolTable;
  }
}
