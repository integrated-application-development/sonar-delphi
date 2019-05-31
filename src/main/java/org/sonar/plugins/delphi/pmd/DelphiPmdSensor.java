/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.delphi.pmd;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSet;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSetNotFoundException;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.lang.ast.ParseException;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.renderers.XMLRenderer;
import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdProfileExporter;
import org.sonar.plugins.delphi.pmd.profile.DelphiRuleSets;
import org.sonar.plugins.delphi.pmd.xml.DelphiRulesUtils;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.delphi.utils.ProgressReporter;
import org.sonar.plugins.delphi.utils.ProgressReporterLogger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * PMD sensor
 */
public class DelphiPmdSensor implements Sensor {

  private final SensorContext context;
  private final DelphiProjectHelper delphiProjectHelper;
  private final List<String> errors = new ArrayList<>();
  private final ActiveRules rulesProfile;

  /**
   * C-tor
   *
   * @param delphiProjectHelper delphiProjectHelper
   * @param context SensorContext
   * @param rulesProfile profile used to export active rules
   */
  public DelphiPmdSensor(DelphiProjectHelper delphiProjectHelper, SensorContext context,
      ActiveRules rulesProfile) {
    this.delphiProjectHelper = delphiProjectHelper;
    this.context = context;
    this.rulesProfile = rulesProfile;
  }

  /**
   * Populate {@link SensorDescriptor} of this sensor.
   */
  @Override
  public void describe(SensorDescriptor descriptor) {
    DelphiUtils.LOG.info("PMD sensor.describe");
    descriptor.name("PMD sensor").onlyOnLanguage(DelphiLanguage.KEY);
  }

  private void addIssue(String ruleKey, String fileName, Integer beginLine, Integer startColumn,
      Integer endLine, String message) {

    DelphiUtils.LOG.debug("PMD Violation - rule: {} file: {} message: {}", ruleKey, fileName,
        message);

    InputFile inputFile = delphiProjectHelper.getFile(fileName);

    NewIssue newIssue = context.newIssue();
    newIssue
        .forRule(RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, ruleKey))
        .at(newIssue.newLocation()
            .on(inputFile)
            .at(inputFile.newRange(beginLine, startColumn, endLine, startColumn + 1))
            .message(message))
        .gap(0.0);
    newIssue.save();
  }

  private void parsePMDreport(File reportFile) {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(reportFile);

      // normalize text representation
      doc.getDocumentElement().normalize();

      NodeList files = doc.getElementsByTagName("file");

      for (int f = 0; f < files.getLength(); f++) {
        Element file = (Element) files.item(f);
        String fileName = file.getAttributes().getNamedItem("name").getTextContent();
        NodeList violations = file.getElementsByTagName("violation");
        for (int n = 0; n < violations.getLength(); n++) {
          Node violation = violations.item(n);
          String beginLine = violation.getAttributes().getNamedItem("beginline").getTextContent();
          String endLine = violation.getAttributes().getNamedItem("endline").getTextContent();
          String beginColumn = violation.getAttributes().getNamedItem("begincolumn")
              .getTextContent();
          String rule = violation.getAttributes().getNamedItem("rule").getTextContent();
          String message = violation.getTextContent();

          try {
            addIssue(rule, fileName, Integer.parseInt(beginLine), Integer.parseInt(beginColumn),
                Integer.parseInt(endLine), message);
          }
          catch (IllegalArgumentException e) {
            DelphiUtils.LOG.info("Failed to add issue:");
            DelphiUtils.LOG.error("Exception Stacktrace", e);
          }
        }
      }
    } catch (SAXParseException e) {
      DelphiUtils.LOG.info("SAXParseException", e);
    } catch (SAXException e) {
      DelphiUtils.LOG.error("SAXException Stacktrace", e);
    } catch (Exception e) {
      DelphiUtils.LOG.info("SAX Parsing Exception");
      DelphiUtils.LOG.error("Exception Stacktrace", e);
    }
  }

  /**
   * The actual sensor code.
   */
  @Override
  public void execute(SensorContext context) {
    DelphiUtils.LOG.info("PMD sensor.execute");
    File reportFile;
    // creating report
    ClassLoader initialClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      reportFile = createPmdReport();
    } finally {
      Thread.currentThread().setContextClassLoader(initialClassLoader);
    }

    parsePMDreport(reportFile);
  }

  private RuleSets createRuleSets() {
    RuleSets rulesets = new DelphiRuleSets();
    String rulesXml = DelphiRulesUtils.exportConfiguration(rulesProfile);
    File ruleSetFile = dumpXmlRuleSet(DelphiPmdConstants.REPOSITORY_KEY, rulesXml);
    RuleSetFactory ruleSetFactory = new RuleSetFactory();

    try {
      RuleSet ruleSet = ruleSetFactory.createRuleSet(ruleSetFile.getAbsolutePath());
      rulesets.addRuleSet(ruleSet);
      return rulesets;
    } catch (RuleSetNotFoundException e) {
      throw new IllegalStateException(e);
    }
  }

  private File dumpXmlRuleSet(String repositoryKey, String rulesXml) {
    try {
      File configurationFile = new File(delphiProjectHelper.workDir(), repositoryKey + ".xml");
      FileUtils.writeStringToFile(configurationFile, rulesXml, Charset.forName("UTF-8"));

      DelphiUtils.LOG.info("PMD configuration: {}", configurationFile.getAbsolutePath());

      return configurationFile;
    } catch (IOException e) {
      throw new IllegalStateException("Fail to save the PMD configuration", e);
    }
  }

  private File createPmdReport() {
    try {
      DelphiPMD pmd = new DelphiPMD();
      RuleContext ruleContext = new RuleContext();
      RuleSets ruleSets = createRuleSets();

      List<File> excluded = delphiProjectHelper.getExcludedSources();

      List<DelphiProject> projects = delphiProjectHelper.getWorkgroupProjects();
      for (DelphiProject delphiProject : projects) {
        DelphiUtils.LOG.info("PMD Parsing project "
            + delphiProject.getName());
        ProgressReporter progressReporter = new ProgressReporter(
            delphiProject.getSourceFiles().size(), 10,
            new ProgressReporterLogger(DelphiUtils.LOG));
        for (File pmdFile : delphiProject.getSourceFiles()) {
          progressReporter.progress();
          if (delphiProjectHelper.isExcluded(pmdFile, excluded)) {
            continue;
          }

          processPmdParse(pmd, ruleContext, ruleSets, pmdFile);
        }
      }

      return writeXmlReport(pmd.getReport());
    } catch (IOException e) {
      DelphiUtils.LOG.error("Could not generate PMD report file.");
      return null;
    }
  }

  private void processPmdParse(DelphiPMD pmd, RuleContext ruleContext, RuleSets ruleSets,
      File pmdFile) {
    try {
      pmd.processFile(pmdFile, ruleSets, ruleContext, delphiProjectHelper.encoding());
    } catch (ParseException e) {
      String errorMsg = "PMD error while parsing " + pmdFile.getAbsolutePath() + ": "
          + e.getMessage();
      DelphiUtils.LOG.warn(errorMsg);
      errors.add(errorMsg);
    }
  }

  /**
   * Generates an XML file from report
   *
   * @param report Report
   * @return XML based on report
   * @throws IOException When report could not be generated
   */
  private File writeXmlReport(Report report)
      throws IOException {
    Renderer xmlRenderer = new XMLRenderer();
    Writer stringWriter = new StringWriter();
    xmlRenderer.setWriter(stringWriter);
    xmlRenderer.start();
    xmlRenderer.renderFileReport(report);
    xmlRenderer.end();

    File xmlReport = new File(delphiProjectHelper.workDir().getAbsolutePath(), "pmd-report.xml");
    DelphiUtils.LOG.info("PMD output report: "
        + xmlReport.getAbsolutePath());
    FileUtils.writeStringToFile(xmlReport, stringWriter.toString());
    return xmlReport;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  public List<String> getErrors() {
    return errors;
  }
}
