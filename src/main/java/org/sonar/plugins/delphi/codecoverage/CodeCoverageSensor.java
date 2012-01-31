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
package org.sonar.plugins.delphi.codecoverage;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.codecoverage.aqtime.AQTimeCoverageParser;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * Sensor that analyses code coverage by connecting to AQTime CC database
 */
public class CodeCoverageSensor implements Sensor {

  private static final String DEFAULT_DRIVER = "net.sourceforge.jtds.jdbc.Driver";
  private static final String PROPERTY_KEYS[]   = {DelphiPlugin.JDBC_DRIVER_KEY, DelphiPlugin.JDBC_URL_KEY, 
    DelphiPlugin.JDBC_USER_KEY, DelphiPlugin.JDBC_PASSWORD_KEY, DelphiPlugin.JDBC_DB_TABLE_PREFIX_KEY};
  private static final String DEFAULT_VALUES[]  = {DEFAULT_DRIVER, "", "", "", "", ""};
  private Map<String, String> jdbcProperties = new HashMap<String, String>();

  public CodeCoverageSensor(Configuration configuration) {
    for(int i = 0; i < PROPERTY_KEYS.length; ++i) {
      jdbcProperties.put(PROPERTY_KEYS[i], configuration.getString(PROPERTY_KEYS[i], DEFAULT_VALUES[i]) );
    }    
  }

  /**
   * {@inheritDoc}
   */

  public boolean shouldExecuteOnProject(Project project) {
    return project.getAnalysisType().isDynamic(true) && DelphiLanguage.KEY.equals(project.getLanguageKey());
  }

  /**
   * {@inheritDoc}
   */

  public void analyse(Project project, SensorContext context) {
    if(areJdbcPropertiesValid()) {
      AQTimeCoverageParser parser = createParser(project);
      parser.parse(project, context);
    }
  }

  private boolean areJdbcPropertiesValid() {
    for(int i = 0; i < PROPERTY_KEYS.length - 1; i++) { //don't include db table prefix
      if(jdbcProperties.get(PROPERTY_KEYS[i]).isEmpty()) {
        DelphiUtils.LOG.warn("Empty jdbc property " + PROPERTY_KEYS[i] + ", code coverage skipped.");
        return false;
      }
    }
    return true;
  }

  private AQTimeCoverageParser createParser(Project project) {
    AQTimeCoverageParser parser = new AQTimeCoverageParser();
    parser.setConnectionProperties(jdbcProperties);
    parser.setSourceFiles(project.getFileSystem().mainFiles(DelphiLanguage.KEY));
    parser.setSourceDirectories(project.getFileSystem().getSourceDirs());
    List<File> excludedDirs = DelphiProjectHelper.getInstance().getCodeCoverageExcludedDirectories(project);
    excludedDirs.addAll(DelphiProjectHelper.getInstance().getExcludedSources(project.getFileSystem())); // we exclude also excluded sources
    parser.setExcludeDirectories(excludedDirs);
    return parser;
  }

  @Override
  public String toString() {
    return "Delphi Code Coverage Sensor";
  }

}
