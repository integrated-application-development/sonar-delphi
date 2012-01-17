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
package org.sonar.plugins.delphi.surefire;

import java.io.File;

import org.sonar.api.batch.AbstractCoverageExtension;
import org.sonar.api.batch.DependsUpon;
import org.sonar.api.batch.Sensor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.resources.Project;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * Surefire sensor used to parse _TRANSFORMED_ DUnit report. You take a DUnit report, then transform it to format that is acceptable by
 * Surefire.
 */
public class SurefireSensor implements Sensor {

  /**
   * @return Sonar's abstract coverage extension class
   */
  @DependsUpon
  public Class<?> dependsUponCoverageSensors() {
    return AbstractCoverageExtension.class;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean shouldExecuteOnProject(Project project) {
    return project.getAnalysisType().isDynamic(true) && DelphiLanguage.KEY.equals(project.getLanguageKey());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void analyse(Project project, SensorContext context) {
    String[] paths = DelphiProjectHelper.getInstance().getSurefireDirectories();

    if (paths == null || paths.length == 0) // no directory was specified
    {
      DelphiUtils.LOG.warn("No Surefire reports directory found!");
      return;
    }

    String mainPath = project.getFileSystem().getBasedir().getAbsolutePath();
    for (String path : paths) // cover each path
    {
      File reportFile = DelphiUtils.resolveAbsolutePath(mainPath, path);
      if ( !reportFile.exists()) { // directory does not exist
        DelphiUtils.LOG.info("surefire report path not found {}", reportFile.getAbsolutePath());
        continue;
      }

      collect(project, context, reportFile);
    }
  }

  protected void collect(Project project, SensorContext context, File reportsDir) {
    DelphiUtils.LOG.info("parsing {}", reportsDir);
    DelphiSureFireParser parser = new DelphiSureFireParser(project, context);
    parser.collect(project, context, reportsDir);
  }

  @Override
  public String toString() {
    return "DelphiLanguage SurefireSensor";
  }
}
