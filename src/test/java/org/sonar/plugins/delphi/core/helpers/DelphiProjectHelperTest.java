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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.internal.DefaultFileSystem;
import org.sonar.api.config.Configuration;
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiProjectHelperTest {
  private static final String PROJECTS_PATH = "/org/sonar/plugins/delphi/projects";
  private final File baseDir = DelphiUtils.getResource(PROJECTS_PATH);
  private Configuration settings;
  private DefaultFileSystem fs;

  private DelphiProject getProject() {
    DelphiProjectHelper delphiProjectHelper = new DelphiProjectHelper(settings, fs);
    List<DelphiProject> projects = delphiProjectHelper.getProjects();
    assertThat(projects, hasSize(1));

    return projects.get(0);
  }

  @Before
  public void setup() {
    fs = new DefaultFileSystem(baseDir);
    settings = mock(Configuration.class);

    String[] includes = {"BadSyntaxProject"};
    when(settings.getStringArray(DelphiPlugin.INCLUDED_DIRECTORIES_KEY)).thenReturn(includes);

    String[] defines = {"DefineFromSettings"};
    when(settings.getStringArray(DelphiPlugin.CONDITIONAL_DEFINES_KEY)).thenReturn(defines);
  }

  @Test
  public void testDefaultProject() {
    DelphiProject project = getProject();

    assertThat(project.getName(), is(DelphiProjectHelper.DEFAULT_PROJECT_NAME));
    assertThat(project.getIncludeDirectories(), hasSize(1));
    assertThat(project.getDefinitions(), hasSize(1));
    assertThat(project.getDefinitions(), hasItem("DefineFromSettings"));
  }

  @Test
  public void testProjectWithoutIncludeDirectories() {
    settings = mock(Configuration.class);

    DelphiProject project = getProject();

    assertThat(project.getIncludeDirectories(), is(empty()));
  }

  @Test
  public void testInvalidIncludesShouldBeSkipped() {
    String[] includes = {"EmptyProject/empty", "BadSyntaxProject", "BadPath/Spooky"};
    when(settings.getStringArray(DelphiPlugin.INCLUDED_DIRECTORIES_KEY)).thenReturn(includes);

    DelphiProject project = getProject();

    assertThat(project.getIncludeDirectories(), hasSize(1));
  }

  @Test
  public void testExcludedDirectories() {
    String[] excludes = {"BadSyntaxProject"};
    when(settings.getStringArray(DelphiPlugin.EXCLUDED_DIRECTORIES_KEY)).thenReturn(excludes);

    DelphiProjectHelper delphiProjectHelper = new DelphiProjectHelper(settings, fs);

    File excluded1 = DelphiUtils.getResource(PROJECTS_PATH + "/BadSyntaxProject/BadSyntax.Pas");
    File excluded2 = DelphiUtils.getResource(PROJECTS_PATH + "/BadSyntaxProject/GoodSyntax.Pas");

    assertThat(delphiProjectHelper.getExcludedDirectories(), hasSize(1));
    assertTrue(delphiProjectHelper.isExcluded(excluded1));
    assertTrue(delphiProjectHelper.isExcluded(excluded2));
  }

  @Test
  public void testDprojProject() {
    String dprojPath = "SimpleProject/dproj/SimpleDelphiProject.dproj";
    when(settings.get(DelphiPlugin.PROJECT_FILE_KEY)).thenReturn(Optional.of(dprojPath));

    DelphiProject project = getProject();

    assertThat(project.getName(), is("Simple Delphi Project"));
    assertThat(project.getIncludeDirectories(), hasSize(2));
    assertThat(project.getSourceFiles(), hasSize(8));
    assertThat(project.getDefinitions(), hasSize(5));
    assertThat(
        project.getDefinitions(),
        hasItems("GGMSGDEBUGx", "LOGTOFILEx", "FullDebugMode", "RELEASE", "DefineFromSettings"));
  }

  @Test
  public void testWorkgroupProject() {
    String workgroupPath = "SimpleProject/dproj/workgroup/All.groupproj";
    when(settings.get(DelphiPlugin.WORKGROUP_FILE_KEY)).thenReturn(Optional.of(workgroupPath));

    DelphiProject project = getProject();

    assertThat(project.getName(), is("Simple Delphi Project"));
    assertThat(project.getIncludeDirectories(), hasSize(2));
    assertThat(project.getSourceFiles(), hasSize(8));
    assertThat(project.getDefinitions(), hasSize(5));
    assertThat(
        project.getDefinitions(),
        hasItems("GGMSGDEBUGx", "LOGTOFILEx", "FullDebugMode", "RELEASE", "DefineFromSettings"));
  }
}
