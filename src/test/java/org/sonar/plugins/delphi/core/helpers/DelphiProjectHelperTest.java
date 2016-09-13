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

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.config.Settings;
import org.sonar.api.resources.Directory;
import org.sonar.api.resources.Project;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;

public class DelphiProjectHelperTest {

  private DelphiProjectHelper delphiProjectHelper;
  private Project project;
  private File currentDir;
  private File baseDir;

  @Before
  public void setup() {
    currentDir = new File(getClass().getResource("/").getPath());
    baseDir = currentDir.getParentFile();

    FileSystem fs = mock(FileSystem.class);
    Settings settings = mock(Settings.class);
    project = mock(Project.class);


    delphiProjectHelper = new DelphiProjectHelper(settings, fs);
  }

  @Test
  public void getDirectory() {
    System.out.println(("THIS IS PROJ:" + project.toString()));
    System.out.println(("THIS IS CURDIR:" + currentDir.toString()));
    System.out.println(("THIS IS BASEDIR:" + baseDir.toString()));

    Directory directory = delphiProjectHelper.getDirectory(currentDir, project);
    assertThat(directory, notNullValue());
    assertThat(directory.getKey(), is("[default]"));
  }

  @Test
  public void getDirectoryEqualsToBaseDir() {
    Directory directory = delphiProjectHelper.getDirectory(baseDir, project);
    assertThat(directory, notNullValue());
    //changed this to macht sonar api 5.0
    assertThat(directory.getKey(), is("[default]"));
  }

  @Test
  public void getInvalidRelativeDirectoryReturnsDefaultPackageName() {
    File rootDirectory = new File("/");
    Directory directory = delphiProjectHelper.getDirectory(rootDirectory, project);
    assertThat(directory, notNullValue());
    assertThat(directory.getKey(), is(DelphiProjectHelper.DEFAULT_PACKAGE_NAME));
  }
}
