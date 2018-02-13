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

import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.delphi.core.DelphiLanguage;
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

  private final Configuration configuration;
  private final DelphiProjectHelper delphiProjectHelper;

  /**
   * Ctor
   * 
   * @param settings Settings provided by Sonar
   * @param delphiProjectHelper The DelphiProjectHelper
   */
  public SurefireSensor(Configuration settings, DelphiProjectHelper delphiProjectHelper) {
    this.configuration = settings;
    this.delphiProjectHelper = delphiProjectHelper;
  }

  @Override
  public void describe(SensorDescriptor descriptor)
  {
    DelphiUtils.LOG.info("SurefireSensor sensor describe...");
    descriptor.name("Delphi SurefireSensor");
    descriptor.onlyOnLanguage(DelphiLanguage.KEY);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  /**
   * The actual sensor code.
   */
  public void execute(SensorContext context)
  {
    DelphiUtils.LOG.info("Delphi sensor execute...");
    String[] paths = configuration.getStringArray(SurefireUtils.SUREFIRE_REPORTS_PATH_PROPERTY);

    if (paths == null || paths.length == 0) {
      DelphiUtils.LOG.warn("No Surefire reports directory found! Using default directory: " + DEFAULT_SUREFIRE_REPORTS_PATH_PROPERTY);
      paths = new String[] {DEFAULT_SUREFIRE_REPORTS_PATH_PROPERTY};
    }

    String mainPath = context.fileSystem().baseDir().getAbsolutePath();
    for (String path : paths) {
      File reportDirectory = DelphiUtils.resolveAbsolutePath(mainPath, path);
      if (!reportDirectory.exists()) {
        DelphiUtils.LOG.warn("surefire report path not found {}", reportDirectory.getAbsolutePath());
        continue;
      }

      collect(context, reportDirectory);
    }
  }

  protected void collect(SensorContext context, File reportsDir) {
    DelphiUtils.LOG.info("parsing {}", reportsDir);
    DelphiSureFireParser parser = new DelphiSureFireParser(delphiProjectHelper);
    parser.collect(context, reportsDir);
  }

  @Override
  public String toString() {
    return "Delphi SurefireSensor";
  }
}
