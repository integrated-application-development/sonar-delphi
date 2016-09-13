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

import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.delphi.colorizer.DelphiColorizerFormat;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.cpd.DelphiCpdMapping;
import org.sonar.plugins.delphi.pmd.DelphiPmdSensor;
import org.sonar.plugins.delphi.pmd.profile.DefaultDelphiProfile;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdProfileExporter;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdProfileImporter;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdRuleDefinition;
import org.sonar.plugins.delphi.surefire.SurefireSensor;

import java.util.ArrayList;
import java.util.List;

/**
 * Main Sonar DelphiLanguage plugin class
 */

@Properties({
  @Property(key = DelphiPlugin.EXCLUDED_DIRECTORIES_KEY, defaultValue = "", name = "Excluded sources",
    description = "List of excluded directories or files, that will not be parsed.", global = true, project = true),
  @Property(key = DelphiPlugin.CC_EXCLUDED_KEY, defaultValue = "", name = "Code coverage excluded directories",
    description = "Code coverage excluded directories list. Files in those directories will not be checked for code coverage.",
    global = true, project = true),
  @Property(key = DelphiPlugin.INCLUDED_DIRECTORIES_KEY, defaultValue = "", name = "Include directories",
    description = "Include directories that will be looked for include files for preprocessor directive {$include}", global = true,
    project = true),
  @Property(key = DelphiPlugin.INCLUDE_EXTEND_KEY, defaultValue = "true", name = "Include extend option",
    description = "Include extend options, can be: 'true' (include files will be processed) or 'false' (turn the feature off)",
    global = true, project = true),
  @Property(
    key = DelphiPlugin.PROJECT_FILE_KEY,
    defaultValue = "",
    name = "Project file",
    description = "Project file. If provided, will be parsed for include lookup path, project source files and preprocessor definitions.",
    global = true, project = true),
  @Property(key = DelphiPlugin.WORKGROUP_FILE_KEY, defaultValue = "", name = "Workgroup file",
    description = "Workgroup file. If provided, will be parsed, then all *.dproj files found in workgroup file will be parsed.",
    global = true, project = true),
  @Property(key = DelphiPlugin.CODECOVERAGE_TOOL_KEY, defaultValue = "delphi code coverage", name = "Code coverage tool",
    description = "Used code coverage tool (AQTime or Delphi Code Coverage)", global = false, project = true),
  @Property(key = DelphiPlugin.CODECOVERAGE_REPORT_KEY, defaultValue = "delphi code coverage report", name = "Code coverage report file",
    description = "Code coverage report to be parsed by Delphi Code Coverage", global = false, project = true),
})
public class DelphiPlugin extends SonarPlugin {

  public static final String EXCLUDED_DIRECTORIES_KEY = "sonar.delphi.sources.excluded";
  public static final String CC_EXCLUDED_KEY = "sonar.delphi.codecoverage.excluded";
  public static final String INCLUDED_DIRECTORIES_KEY = "sonar.delphi.sources.include";
  public static final String INCLUDE_EXTEND_KEY = "sonar.delphi.sources.include.extend";
  public static final String PROJECT_FILE_KEY = "sonar.delphi.sources.project";
  public static final String WORKGROUP_FILE_KEY = "sonar.delphi.sources.workgroup";
  public static final String CODECOVERAGE_TOOL_KEY = "sonar.delphi.codecoverage.tool";
  public static final String CODECOVERAGE_REPORT_KEY = "sonar.delphi.codecoverage.report";

  /**
   * {@inheritDoc}
   */

  @Override
  public List<Class> getExtensions() {
    List<Class> list = new ArrayList<Class>();

    // Sensors
    list.add(DelphiSensor.class);
    // Core
    list.add(DelphiLanguage.class);
    list.add(DelphiCpdMapping.class);
    // Core helpers
    list.add(DelphiProjectHelper.class);
    // Colorizer
    list.add(DelphiColorizerFormat.class);
    // Surefire
    list.add(SurefireSensor.class);
    // Pmd
    list.add(DelphiPmdSensor.class);
    list.add(DelphiPmdRuleDefinition.class);
    list.add(DefaultDelphiProfile.class);
    list.add(DelphiPmdProfileExporter.class);
    list.add(DelphiPmdProfileImporter.class);

    return list;
  }

}
