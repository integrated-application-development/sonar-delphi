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
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.ast.ParseException;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.renderers.XMLRenderer;
import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.resources.Project;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.pmd.profile.DelphiRuleSets;
import org.sonar.plugins.delphi.pmd.xml.DelphiPmdXmlReportParser;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.delphi.utils.ProgressReporter;
import org.sonar.plugins.delphi.utils.ProgressReporterLogger;

/**
 * PMD sensor
 */
public class DelphiPmdSensor implements Sensor {

  private final ResourcePerspectives perspectives;
  private final DelphiProjectHelper delphiProjectHelper;
  private final List<String> errors = new ArrayList<String>();

  /**
   * C-tor
   */
  public DelphiPmdSensor(DelphiProjectHelper delphiProjectHelper, ResourcePerspectives perspectives) {
    this.delphiProjectHelper = delphiProjectHelper;
    this.perspectives = perspectives;
  }

  /**
   * Analyses a project
   */

  @Override
  public void analyse(Project project, SensorContext context) {
    // creating report
    File reportFile = createPmdReport(project);

    // analysing report
    DelphiPmdXmlReportParser parser = new DelphiPmdXmlReportParser(delphiProjectHelper, perspectives);
    parser.parse(reportFile);
  }

  private RuleSets createRuleSets() {
    RuleSets rulesets = new DelphiRuleSets();
    RuleSetFactory ruleSetFactory = new RuleSetFactory();
    rulesets.addRuleSet(ruleSetFactory
      .createRuleSet(getClass().getResourceAsStream(
        "/org/sonar/plugins/delphi/pmd/rules.xml")));
    return rulesets;
  }

  private File createPmdReport(Project project) {
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

      return writeXmlReport(project, pmd.getReport());
    } catch (IOException e) {
      DelphiUtils.LOG.error("Could not generate PMD report file.");
      return null;
    }
  }

  /**
   * Generates an XML file from report
   * 
   * @param project Project
   * @param report Report
   * @return XML based on report
   * @throws IOException When report could not be generated
   */
  private File writeXmlReport(Project project, Report report)
    throws IOException {
    Renderer xmlRenderer = new XMLRenderer();
    Writer stringwriter = new StringWriter();
    xmlRenderer.setWriter(stringwriter);
    xmlRenderer.start();
    xmlRenderer.renderFileReport(report);
    xmlRenderer.end();

    File xmlReport = new File(delphiProjectHelper.workDir().getAbsolutePath(), "pmd-report.xml");
    DelphiUtils.LOG.info("PMD output report: "
      + xmlReport.getAbsolutePath());
    FileUtils.writeStringToFile(xmlReport, stringwriter.toString());
    return xmlReport;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return delphiProjectHelper.shouldExecuteOnProject();
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  public List<String> getErrors() {
    return errors;
  }

}
