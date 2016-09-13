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
package org.sonar.plugins.delphi.surefire;

import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Project;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.surefire.api.SurefireUtils;

import java.io.File;

/**
 * Surefire sensor used to parse _TRANSFORMED_ DUnit report. You take a DUnit
 * report, then transform it to format that is acceptable by Surefire.
 */
public class SurefireSensor implements Sensor {

  private static final String DEFAULT_SUREFIRE_REPORTS_PATH_PROPERTY = "target/surefire-reports";

  private final Settings settings;
  private final DelphiProjectHelper delphiProjectHelper;

  /**
   * Ctor
   * 
   * @param settings Settings provided by Sonar
   * @param delphiProjectHelper The DelphiProjectHelper
   */
  public SurefireSensor(Settings settings, DelphiProjectHelper delphiProjectHelper) {
    this.settings = settings;
    this.delphiProjectHelper = delphiProjectHelper;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return delphiProjectHelper.shouldExecuteOnProject();
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public void analyse(Project project, SensorContext context) {
    String[] paths = settings.getStringArray(SurefireUtils.SUREFIRE_REPORTS_PATH_PROPERTY);

    if (paths == null || paths.length == 0) {
      DelphiUtils.LOG.warn("No Surefire reports directory found! Using default directory: " + DEFAULT_SUREFIRE_REPORTS_PATH_PROPERTY);
      paths = new String[] {DEFAULT_SUREFIRE_REPORTS_PATH_PROPERTY};
    }

    String mainPath = delphiProjectHelper.baseDir().getAbsolutePath();
    for (String path : paths) {
      File reportDirectory = DelphiUtils.resolveAbsolutePath(mainPath, path);
      if (!reportDirectory.exists()) {
        DelphiUtils.LOG.warn("surefire report path not found {}", reportDirectory.getAbsolutePath());
        continue;
      }

      collect(project, context, reportDirectory);
    }
  }

  protected void collect(Project project, SensorContext context, File reportsDir) {
    DelphiUtils.LOG.info("parsing {}", reportsDir);
    DelphiSureFireParser parser = new DelphiSureFireParser(delphiProjectHelper);
    parser.collect(project, context, reportsDir);
  }

  @Override
  public String toString() {
    return "Delphi SurefireSensor";
  }
}
