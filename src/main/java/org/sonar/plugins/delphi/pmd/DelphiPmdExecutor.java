package org.sonar.plugins.delphi.pmd;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.pmd.PMDVersion;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.lang.ast.ParseException;
import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.scanner.ScannerSide;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.api.utils.log.Profiler;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStream;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStreamConfig;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.pmd.profile.DelphiRuleSets;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSet;
import org.sonar.plugins.delphi.pmd.xml.DelphiRuleSetHelper;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.ProgressReporter;
import org.sonar.plugins.delphi.utils.ProgressReporterLogger;

/**
 * PMD sensor
 */
@ScannerSide
public class DelphiPmdExecutor {
  private static final Logger LOGGER = Loggers.get(DelphiPmdExecutor.class);
  private final DelphiProjectHelper delphiProjectHelper;
  private final ActiveRules rulesProfile;
  private final DelphiPmdConfiguration pmdConfiguration;

  private DelphiFileStreamConfig fileStreamConfig;
  private List<String> errors = new ArrayList<>();

  /**
   * Constructor
   *
   * @param delphiProjectHelper delphiProjectHelper
   * @param rulesProfile profile used to export active rules
   * @param pmdConfiguration a helper class for PMD report/ruleset configuration and manipulation
   */
  public DelphiPmdExecutor(DelphiProjectHelper delphiProjectHelper,
      ActiveRules rulesProfile, DelphiPmdConfiguration pmdConfiguration) {
    this.delphiProjectHelper = delphiProjectHelper;
    this.rulesProfile = rulesProfile;
    this.pmdConfiguration = pmdConfiguration;
  }

  public Report execute() {
    final Profiler profiler = Profiler.create(LOGGER).startInfo(
        "Execute PMD " + PMDVersion.VERSION);

    Report result = executePmd();

    profiler.stopInfo();
    return result;
  }

  private Report executePmd() {
    DelphiPMD pmd = new DelphiPMD();
    Report report = pmd.getReport();
    RuleContext ruleContext = new RuleContext();
    RuleSets ruleSets = createRuleSets(DelphiPmdConstants.REPOSITORY_KEY);
    List<DelphiProject> projects = delphiProjectHelper.getProjects();

    for (DelphiProject delphiProject : projects) {
      fileStreamConfig = DelphiFileStream.createConfig(delphiProject, delphiProjectHelper);
      addToPmdReport(delphiProject, pmd, ruleContext, ruleSets);
    }

    pmdConfiguration.dumpXmlReport(report);

    return report;
  }

  private void addToPmdReport(DelphiProject delphiProject, DelphiPMD pmd, RuleContext ruleContext,
      RuleSets ruleSets) {
    DelphiPlugin.LOG.info("PMD Parsing project {}", delphiProject.getName());

    List<File> excluded = delphiProjectHelper.getExcludedSources();
    ProgressReporter progressReporter = new ProgressReporter(
        delphiProject.getSourceFiles().size(), 10, new ProgressReporterLogger(DelphiPlugin.LOG));

    for (File pmdFile : delphiProject.getSourceFiles()) {
      progressReporter.progress();
      if (!delphiProjectHelper.isExcluded(pmdFile, excluded)) {
        processPmdParse(pmd, ruleContext, ruleSets, pmdFile);
      }
    }
  }

  private RuleSets createRuleSets(String repositoryKey) {
    RuleSets rulesets = new DelphiRuleSets();
    String rulesXml = dumpXml(rulesProfile, repositoryKey);
    File ruleSetFile = dumpXmlRuleSet(repositoryKey, rulesXml);
    RuleSetFactory ruleSetFactory = new RuleSetFactory();

    try {
      RuleSet ruleSet = ruleSetFactory.createRuleSet(ruleSetFile.getAbsolutePath());
      rulesets.addRuleSet(ruleSet);
      return rulesets;
    } catch (RuleSetNotFoundException e) {
      throw new IllegalStateException(e);
    }
  }

  private String dumpXml(ActiveRules rulesProfile, String repositoryKey) {
    final StringWriter writer = new StringWriter();
    final DelphiRuleSet ruleSet = DelphiRuleSetHelper.createFrom(rulesProfile, repositoryKey);
    ruleSet.writeTo(writer);

    return writer.toString();
  }


  private File dumpXmlRuleSet(String repositoryKey, String rulesXml) {
    try {
      File configurationFile = new File(delphiProjectHelper.workDir(), repositoryKey + ".xml");
      FileUtils.writeStringToFile(configurationFile, rulesXml, StandardCharsets.UTF_8);

      DelphiPlugin.LOG.info("PMD configuration: {}", configurationFile.getAbsolutePath());

      return configurationFile;
    } catch (IOException e) {
      throw new IllegalStateException("Fail to save the PMD configuration", e);
    }
  }

  private void processPmdParse(DelphiPMD pmd, RuleContext ruleContext, RuleSets ruleSets,
      File pmdFile) {
    try {
      pmd.processFile(pmdFile, ruleSets, ruleContext, fileStreamConfig);
    } catch (ParseException e) {
      String errorMsg = "PMD error while parsing " + pmdFile.getAbsolutePath() + ": "
          + e.getMessage();
      DelphiPlugin.LOG.warn(errorMsg);
      errors.add(errorMsg);
    }
  }


  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  public List<String> getErrors() {
    return errors;
  }
}
