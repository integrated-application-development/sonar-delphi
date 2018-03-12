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

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import net.sourceforge.pmd.*;
import net.sourceforge.pmd.lang.ast.ParseException;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.renderers.XMLRenderer;
import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.InputModule;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rule.RuleKey;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdProfileExporter;
import org.sonar.plugins.delphi.pmd.profile.DelphiRuleSets;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.delphi.utils.ProgressReporter;
import org.sonar.plugins.delphi.utils.ProgressReporterLogger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * PMD sensor
 */
public class DelphiPmdSensor implements Sensor {

  private final SensorContext context;
  private final DelphiProjectHelper delphiProjectHelper;
  private final List<String> errors = new ArrayList<>();
  private final DelphiPmdProfileExporter profileExporter;
  private final RulesProfile rulesProfile;

  /**
   * C-tor
   *
   * @param delphiProjectHelper delphiProjectHelper
   * @param context             SensorContext
   * @param rulesProfile        rulesProfile used to export active rules
   * @param profileExporter     used to export active rules
   */
  public DelphiPmdSensor(DelphiProjectHelper delphiProjectHelper, SensorContext context, RulesProfile rulesProfile, DelphiPmdProfileExporter profileExporter) {
    this.delphiProjectHelper = delphiProjectHelper;
    this.context = context;
    this.rulesProfile = rulesProfile;
    this.profileExporter = profileExporter;
  }

  /**
   * Populate {@link SensorDescriptor} of this sensor.
   */
  @Override
  public void describe(SensorDescriptor descriptor) {
    DelphiUtils.LOG.info("PMD sensor.describe");
    descriptor.name("PMD sensor").onlyOnLanguage(DelphiLanguage.KEY);
  }

  private void addIssue(String ruleKey, String fileName, Integer beginLine, Integer startColumn, Integer endLine,
                        String message, Integer priority) {

    DelphiUtils.LOG.debug("PMD Violation - rule: " + ruleKey + " file: " + fileName + " message: " + message);

    InputFile inputFile = delphiProjectHelper.getFile(fileName);

    NewIssue newIssue = context.newIssue();
    newIssue
            .forRule(RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, ruleKey))
            .at(newIssue.newLocation()
                    .on(inputFile)
                    .at(inputFile.newRange(beginLine, startColumn, endLine, startColumn +1))
                    .message(message))
            .gap(0.0);
    newIssue.save();
  }

  void parsePMDreport(File reportFile)
  {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
       DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
       Document doc = docBuilder.parse(reportFile);

       // normalize text representation
       doc.getDocumentElement().normalize();

       NodeList files = doc.getElementsByTagName("file");

       for (int f = 0; f < files.getLength(); f++) {
         Element file = (Element)files.item(f);
         String fileName = file.getAttributes().getNamedItem("name").getTextContent();
         NodeList violations = file.getElementsByTagName("violation");
         for (int n = 0; n < violations.getLength(); n++)
         {
           Node violation = violations.item(n);
           String beginLine = violation.getAttributes().getNamedItem("beginline").getTextContent();
           String endLine = violation.getAttributes().getNamedItem("endline").getTextContent();
           String beginColumn = violation.getAttributes().getNamedItem("begincolumn").getTextContent();
           String endColumn = violation.getAttributes().getNamedItem("endcolumn").getTextContent();
           String rule = violation.getAttributes().getNamedItem("rule").getTextContent();
           String priority = violation.getAttributes().getNamedItem("priority").getTextContent();
           String message = violation.getTextContent();
           addIssue(rule, fileName, Integer.parseInt(beginLine), Integer.parseInt(beginColumn),
             Integer.parseInt(endLine), message, Integer.parseInt(priority));
         }
       }
    } catch (SAXParseException err) {
      DelphiUtils.LOG.info("SAXParseException");
    } catch (SAXException e) {
      DelphiUtils.LOG.info("SAXException");
       Exception x = e.getException ();
       ((x == null) ? e : x).printStackTrace ();
    } catch (Throwable t) {
      DelphiUtils.LOG.info("Throwable");
       t.printStackTrace ();
    }
  }

  /**
   * The actual sensor code.
   */
  @Override
  public void execute(SensorContext context)
  {
    DelphiUtils.LOG.info("PMD sensor.execute");
    File reportFile;
    // creating report
    ClassLoader initialClassLoader = Thread.currentThread().getContextClassLoader();
    try {
      Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
      reportFile = createPmdReport(context.module());
    } finally {
      Thread.currentThread().setContextClassLoader(initialClassLoader);
    }

    parsePMDreport(reportFile);
  }

  private RuleSets createRuleSets() {
    RuleSets rulesets = new DelphiRuleSets();
    String rulesXml = profileExporter.exportProfileToString(rulesProfile);
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
      Files.asCharSink(configurationFile, Charsets.UTF_8).write(rulesXml);

      DelphiUtils.LOG.info("PMD configuration: " + configurationFile.getAbsolutePath());

      return configurationFile;
    } catch (IOException e) {
      throw new IllegalStateException("Fail to save the PMD configuration", e);
    }
  }

  private File createPmdReport(InputModule module) {
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

          try {
            pmd.processFile(pmdFile, ruleSets, ruleContext, delphiProjectHelper.encoding());
          } catch (ParseException e) {
            String errorMsg = "PMD error while parsing " + pmdFile.getAbsolutePath() + ": "
              + e.getMessage();
            DelphiUtils.LOG.warn(errorMsg);
            errors.add(errorMsg);
          }
        }
      }

      return writeXmlReport(pmd.getReport());
    } catch (IOException e) {
      DelphiUtils.LOG.error("Could not generate PMD report file.");
      return null;
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
