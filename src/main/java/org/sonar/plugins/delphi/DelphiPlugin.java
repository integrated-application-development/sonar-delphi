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
package org.sonar.plugins.delphi;

import java.util.ArrayList;
import java.util.List;

import org.sonar.api.Extension;
import org.sonar.api.Properties;
import org.sonar.api.Property;
import org.sonar.api.SonarPlugin;
import org.sonar.plugins.delphi.codecoverage.CodeCoverageSensor;
import org.sonar.plugins.delphi.colorizer.DelphiColorizerFormat;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.cpd.DelphiCpdMapping;
import org.sonar.plugins.delphi.pmd.DelphiPmdSensor;
import org.sonar.plugins.delphi.pmd.profile.DefaultDelphiProfile;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdProfileExporter;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdProfileImporter;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdRuleRepository;
import org.sonar.plugins.delphi.surefire.SurefireSensor;

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
    @Property(key = DelphiPlugin.JDBC_DRIVER_KEY, defaultValue = "net.sourceforge.jtds.jdbc.Driver", name = "JDBC Driver",
        description = "Class name for JDBC driver.", global = true, project = true),
    @Property(key = DelphiPlugin.JDBC_URL_KEY, defaultValue = "", name = "Connection host url", description = "Database host url",
        global = true, project = true),
    @Property(key = DelphiPlugin.JDBC_USER_KEY, defaultValue = "", name = "User name", description = "Database user name", global = true,
        project = true),
    @Property(key = DelphiPlugin.JDBC_PASSWORD_KEY, defaultValue = "", name = "User password", description = "Database user password",
        global = true, project = true),
    @Property(key = DelphiPlugin.JDBC_DB_TABLE_PREFIX_KEY, defaultValue = "", name = "AQTime database table prefix",
        description = "AQTime database table prefix", global = true, project = true),
    @Property(key = DelphiPlugin.TEST_DIRECTORIES_KEY, defaultValue = "", name = "Test directories",
        description = "List of unit test directories", global = true, project = true) })
public class DelphiPlugin extends SonarPlugin {

  public static final String EXCLUDED_DIRECTORIES_KEY = "sonar.delphi.sources.excluded";
  public static final String CC_EXCLUDED_KEY = "sonar.delphi.codecoverage.excluded";
  public static final String INCLUDED_DIRECTORIES_KEY = "sonar.delphi.sources.include";
  public static final String TEST_DIRECTORIES_KEY = "sonar.delphi.sources.tests";
  public static final String INCLUDE_EXTEND_KEY = "sonar.delphi.sources.include.extend";
  public static final String PROJECT_FILE_KEY = "sonar.delphi.sources.project";
  public static final String WORKGROUP_FILE_KEY = "sonar.delphi.sources.workgroup";
  public static final String JDBC_DRIVER_KEY = "sonar.delphi.codecoverage.aqtime.jdbc.driver";
  public static final String JDBC_URL_KEY = "sonar.delphi.codecoverage.aqtime.jdbc.url";
  public static final String JDBC_USER_KEY = "sonar.delphi.codecoverage.aqtime.jdbc.user";
  public static final String JDBC_PASSWORD_KEY = "sonar.delphi.codecoverage.aqtime.jdbc.password";
  public static final String JDBC_DB_TABLE_PREFIX_KEY = "sonar.delphi.codecoverage.aqtime.jdbc.prefix";

  /**
   * {@inheritDoc}
   */

  public List<Class<? extends Extension>> getExtensions() {
    List<Class<? extends Extension>> list = new ArrayList<Class<? extends Extension>>();

    // Sensors
    list.add(DelphiSensor.class);
    // Core
    list.add(DelphiLanguage.class);
    list.add(DelphiCpdMapping.class);
    // Core helpers
    list.add(DelphiProjectHelper.class);
    // Colorizer
    list.add(DelphiColorizerFormat.class);
    // Code Coverage Sensor
    list.add(CodeCoverageSensor.class);
    // Surefire
    list.add(SurefireSensor.class);
    // Pmd
    list.add(DelphiPmdSensor.class);
    list.add(DelphiPmdRuleRepository.class);
    list.add(DefaultDelphiProfile.class);
    list.add(DelphiPmdProfileExporter.class);
    list.add(DelphiPmdProfileImporter.class);

    return list;
  }

}
