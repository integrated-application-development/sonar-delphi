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

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.surefire.api.SurefireUtils;
import org.sonar.api.config.internal.MapSettings;

import java.io.File;
import java.io.FilenameFilter;

import static org.junit.Assert.assertEquals;

public class SurefireSensorTest {

  private static final String PROJECT_DIR = "/org/sonar/plugins/delphi/UnitTest";
  private static final String PROJECT_TEST_DIR = "/org/sonar/plugins/delphi/UnitTest/tests";
  private static final String SUREFIRE_REPORT_DIR = "./reports";

  private MapSettings settings;
  private DelphiProjectHelper delphiProjectHelper;
  private SensorContextTester sensorContext;

  @Before
  public void setup()
  {
    File baseDir = DelphiUtils.getResource(PROJECT_TEST_DIR);

    sensorContext = SensorContextTester.create(DelphiUtils.getResource(PROJECT_DIR));

    delphiProjectHelper = new DelphiProjectHelper(sensorContext.config(), sensorContext.fileSystem());

    File[] unitTestFiles =  baseDir.listFiles(new FilenameFilter() {
    @Override
    public boolean accept(File dir, String name) {
      return name.endsWith(".pas");
    }
    });

    for (File unitTestFile : unitTestFiles) {
      InputFile inputFile = TestInputFileBuilder.create("",
          baseDir, unitTestFile)
          .setLanguage(DelphiLanguage.KEY)
          .setType(InputFile.Type.TEST)
          .build();
      sensorContext.fileSystem().add(inputFile);
    }

    settings = new MapSettings();
  }

  @Test
  public void executeTest() {
    settings.setProperty(SurefireUtils.SUREFIRE_REPORT_PATHS_PROPERTY, SUREFIRE_REPORT_DIR);
    SurefireSensor sensor = new SurefireSensor(settings.asConfig(), delphiProjectHelper);
    sensor.execute(sensorContext);

    assertEquals(6, sensorContext.measures(":MyTest1.pas").size());
    assertEquals(6, sensorContext.measures(":MyTest2.pas").size());
  }

  @Test
  public void executeTestUsingDefaultSurefireReportsPath() {
    SurefireSensor sensor = new SurefireSensor(settings.asConfig(), delphiProjectHelper);
    sensor.execute(sensorContext);

    assertEquals(0, sensorContext.measures(":MyTest1.pas").size());
    assertEquals(0, sensorContext.measures(":MyTest2.pas").size());
  }
}
