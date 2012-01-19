/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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
import java.util.List;

import net.sourceforge.pmd.Report;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleSetFactory;
import net.sourceforge.pmd.RuleSets;
import net.sourceforge.pmd.renderers.Renderer;
import net.sourceforge.pmd.renderers.XMLRenderer;

import org.apache.commons.io.FileUtils;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.rules.RuleFinder;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.pmd.profile.DelphiRuleSets;
import org.sonar.plugins.delphi.pmd.xml.DelphiPmdXmlReportParser;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * PMD sensor
 */
public class DelphiPmdSensor implements Sensor {

  private RuleFinder rulesFinder;

  /**
   * C-tor
   */
  public DelphiPmdSensor(RuleFinder rulesFinder) {
    this.rulesFinder = rulesFinder;
  }

  /**
   * Analyses a project
   */

  public void analyse(Project project, SensorContext context) {
    // creating report
    DelphiUtils.getDebugLog().println(">> PMD STARTING");
    File reportFile = createPmdReport(project);

    // analysing report
    DelphiPmdXmlReportParser parser = getStaxParser(project, context);
    parser.parse(reportFile);
  }

  private RuleSets createRuleSets() {
    RuleSets rulesets = new DelphiRuleSets();
    RuleSetFactory ruleSetFactory = new RuleSetFactory();
    rulesets.addRuleSet(ruleSetFactory.createRuleSet(getClass().getResourceAsStream("/org/sonar/plugins/delphi/pmd/rules.xml")));
    return rulesets;
  }

  private File createPmdReport(Project project) {
    try {
      DelphiPMD pmd = new DelphiPMD();
      RuleContext ruleContext = new RuleContext();
      RuleSets ruleSets = createRuleSets();

      // excluded files
      ProjectFileSystem fileSystem = project.getFileSystem();
      List<File> excluded = DelphiProjectHelper.getInstance().getExcludedSources(fileSystem);

      List<DelphiProject> projects = DelphiProjectHelper.getInstance().getWorkgroupProjects(project); // get workspace projects
      for (DelphiProject delphiProject : projects) // for every .dproj file
      {
        DelphiUtils.LOG.info("PMD Parsing project " + delphiProject.getName());
        for (File pmdFile : delphiProject.getSourceFiles()) {
          if (DelphiProjectHelper.getInstance().isExcluded(pmdFile, excluded)) {
            continue;
          }
          pmd.processFile(pmdFile, ruleSets, ruleContext);
        }

      }

      // write xml report
      return writeXmlReport(project, pmd.getReport());
    } catch (IOException e) {
      DelphiUtils.LOG.error("Could not generate PMD report file.");
      DelphiUtils.getDebugLog().println("Could not generate PMD report file.");
      return null;
    }
  }

  /**
   * Generates an XML file from report
   * 
   * @param project
   *          Project
   * @param report
   *          Report
   * @return XML based on report
   * @throws IOException
   *           When report could not be generated
   */
  private File writeXmlReport(Project project, Report report) throws IOException {
    Renderer xmlRenderer = new XMLRenderer();
    Writer stringwriter = new StringWriter();
    xmlRenderer.setWriter(stringwriter);
    xmlRenderer.start();
    xmlRenderer.renderFileReport(report);
    xmlRenderer.end();

    File xmlReport = new File(project.getFileSystem().getSonarWorkingDirectory(), "pmd-report.xml");
    DelphiUtils.LOG.info("PMD output report: " + xmlReport.getAbsolutePath());
    FileUtils.writeStringToFile(xmlReport, stringwriter.toString());
    return xmlReport;
  }

  /**
   * {@inheritDoc}
   */

  public boolean shouldExecuteOnProject(Project project) {
    return DelphiLanguage.KEY.equals(project.getLanguageKey());
  }

  private DelphiPmdXmlReportParser getStaxParser(Project project, SensorContext context) {
    return new DelphiPmdXmlReportParser(project, context, rulesFinder);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
