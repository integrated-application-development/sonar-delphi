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
package org.sonar.plugins.delphi.core.helpers;

import static org.mockito.Mockito.mock;

import java.io.File;
import org.junit.Before;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.config.Configuration;

public class DelphiProjectHelperTest {

  private DelphiProjectHelper delphiProjectHelper;
  private SensorContext sensorContext;
  private File currentDir;
  private File baseDir;

  //TODO: Add tests
  @Before
  public void setup() {
    currentDir = new File(getClass().getResource("/").getPath());
    baseDir = currentDir.getParentFile();

    FileSystem fs = mock(FileSystem.class);
    Configuration settings = mock(Configuration.class);
    sensorContext = mock(SensorContext.class);

    delphiProjectHelper = new DelphiProjectHelper(settings, fs);
  }

}
