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
package org.sonar.plugins.delphi;

import org.sonar.api.Plugin;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.executor.DelphiCpdExecutor;
import org.sonar.plugins.delphi.executor.DelphiHighlightExecutor;
import org.sonar.plugins.delphi.executor.DelphiMasterExecutor;
import org.sonar.plugins.delphi.executor.DelphiMetricsExecutor;
import org.sonar.plugins.delphi.executor.DelphiPmdExecutor;
import org.sonar.plugins.delphi.pmd.DelphiPmdConfiguration;
import org.sonar.plugins.delphi.pmd.profile.DefaultDelphiProfile;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdProfileExporter;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdProfileImporter;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdRuleSetDefinitionProvider;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdRulesDefinition;
import org.sonar.plugins.delphi.pmd.violation.DelphiPmdViolationRecorder;
import org.sonar.plugins.delphi.surefire.SurefireSensor;

/** Main Sonar DelphiLanguage plugin class */
@Properties({
  @Property(
      key = DelphiPlugin.EXCLUDED_DIRECTORIES_KEY,
      name = "Excluded sources",
      description = "List of excluded directories or files, that will not be parsed.",
      project = true,
      multiValues = true),
  @Property(
      key = DelphiPlugin.CC_EXCLUDED_KEY,
      name = "Code coverage excluded directories",
      description =
          "Code coverage excluded directories list. Files in those "
              + "directories will not be checked for code coverage.",
      project = true),
  @Property(
      key = DelphiPlugin.INCLUDED_DIRECTORIES_KEY,
      name = "Include directories",
      description =
          "Include directories that will be looked for include files for "
              + "preprocessor directive {$include}",
      project = true,
      multiValues = true),
  @Property(
      key = DelphiPlugin.INCLUDE_EXTEND_KEY,
      defaultValue = "true",
      name = "Include extend option",
      description =
          "Include extend options, can be: 'true' (include files will be processed) "
              + "or 'false' (turn the feature off)",
      project = true),
  @Property(
      key = DelphiPlugin.PROJECT_FILE_KEY,
      name = "Project file",
      description =
          "Project file. If provided, will be parsed for include lookup path, "
              + "project source files and preprocessor definitions.",
      project = true),
  @Property(
      key = DelphiPlugin.WORKGROUP_FILE_KEY,
      name = "Workgroup file",
      description =
          "Workgroup file. If provided, will be parsed, then all "
              + "*.dproj files found in workgroup file will be parsed.",
      project = true),
  @Property(
      key = DelphiPlugin.CONDITIONAL_DEFINES_KEY,
      name = "Conditional Defines",
      description = "List of conditional defines to define while parsing the project",
      project = true,
      multiValues = true),
  @Property(
      key = DelphiPlugin.CODECOVERAGE_TOOL_KEY,
      defaultValue = "delphi code coverage",
      name = "Code coverage tool",
      description = "Used code coverage tool (AQTime or Delphi Code Coverage)",
      project = true,
      global = false),
  @Property(
      key = DelphiPlugin.CODECOVERAGE_REPORT_KEY,
      defaultValue = "delphi code coverage report",
      name = "Code coverage report file",
      description = "Code coverage report to be parsed by Delphi Code Coverage",
      project = true,
      global = false),
  @Property(
      key = DelphiPlugin.GENERATE_PMD_REPORT_XML_KEY,
      defaultValue = "false",
      name = "Generate XML Report",
      description = "Whether a PMD Report XML file should be generated",
      project = true,
      global = false),
  @Property(
      key = DelphiPlugin.TEST_TYPE_REGEX_KEY,
      defaultValue = "(?!)",
      name = "Test-Harness type regex",
      description =
          "Rules can be configured not to apply to test code. "
              + "A type name that matches this regex will have its methods considered as test code",
      project = true),
})
public class DelphiPlugin implements Plugin {
  public static final String EXCLUDED_DIRECTORIES_KEY = "sonar.delphi.sources.excluded";
  public static final String CC_EXCLUDED_KEY = "sonar.delphi.codecoverage.excluded";
  public static final String INCLUDED_DIRECTORIES_KEY = "sonar.delphi.sources.include";
  public static final String INCLUDE_EXTEND_KEY = "sonar.delphi.sources.include.extend";
  public static final String PROJECT_FILE_KEY = "sonar.delphi.sources.project";
  public static final String WORKGROUP_FILE_KEY = "sonar.delphi.sources.workgroup";
  public static final String CONDITIONAL_DEFINES_KEY = "sonar.delphi.conditionalDefines";
  public static final String CODECOVERAGE_TOOL_KEY = "sonar.delphi.codecoverage.tool";
  public static final String CODECOVERAGE_REPORT_KEY = "sonar.delphi.codecoverage.report";
  public static final String GENERATE_PMD_REPORT_XML_KEY = "sonar.delphi.pmd.generateXml";
  public static final String TEST_TYPE_REGEX_KEY = "sonar.delphi.pmd.testTypeRegex";

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }

  @Override
  public void define(Context context) {
    context.addExtensions(
        // Sensors
        DelphiSensor.class,
        SurefireSensor.class,
        // Executors
        DelphiMasterExecutor.class,
        DelphiPmdExecutor.class,
        DelphiMetricsExecutor.class,
        DelphiCpdExecutor.class,
        DelphiHighlightExecutor.class,
        // Core
        DelphiLanguage.class,
        // Core helpers
        DelphiProjectHelper.class,
        // PMD
        DelphiPmdConfiguration.class,
        DelphiPmdRulesDefinition.class,
        DelphiPmdRuleSetDefinitionProvider.class,
        DefaultDelphiProfile.class,
        DelphiPmdProfileExporter.class,
        DelphiPmdProfileImporter.class,
        DelphiPmdViolationRecorder.class);
  }
}
