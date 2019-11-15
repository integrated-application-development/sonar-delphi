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
package org.sonar.plugins.delphi.project;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiProjectTest {

  private static final String XML_FILE =
      "/org/sonar/plugins/delphi/projects/SimpleProject/dproj/SimpleDelphiProject.dproj";
  private static final String INC_DIR =
      "/org/sonar/plugins/delphi/projects/SimpleProject/includes1";

  private DelphiProject project;

  @Before
  public void init() {
    project = new DelphiProject("simple project");
  }

  @Test
  public void testSimpleProject() throws IOException {
    File sourceFile = File.createTempFile("tempfile", ".pas");
    sourceFile.deleteOnExit();

    assertThat(project.getName()).isEqualTo("simple project");
    assertThat(project.getDefinitions().size()).isEqualTo(0);
    assertThat(project.getIncludeDirectories().size()).isEqualTo(0);
    assertThat(project.getSourceFiles().size()).isEqualTo(0);
    assertThat(project.getXmlFile()).isNull();

    project.addDefinition("DEF");
    assertThat(project.getDefinitions().size()).isEqualTo(1);
    project.addFile(DelphiUtils.getResource(XML_FILE).getAbsolutePath());
    assertThat(project.getSourceFiles().size()).isEqualTo(0);
    project.addFile(sourceFile.getAbsolutePath());
    assertThat(project.getSourceFiles().size()).isEqualTo(1);
    project.addIncludeDirectory(DelphiUtils.getResource(INC_DIR).getAbsolutePath());
    assertThat(project.getIncludeDirectories().size()).isEqualTo(1);
  }

  @Test
  public void testSetDefinitions() {
    List<String> defs = new ArrayList<>();
    project.setDefinitions(defs);
    assertThat(project.getDefinitions()).isEqualTo(defs);
  }

  @Test
  public void testSetFile() {
    File file = DelphiUtils.getResource(XML_FILE);
    project.setFile(file);
    assertThat(project.getXmlFile()).isEqualTo(file);
  }

  @Test
  public void testParseFile() throws Exception {
    project = new DelphiProject(DelphiUtils.getResource(XML_FILE));

    assertThat(project.getName()).isEqualTo("Simple Delphi Project");

    assertThat(project.getSourceFiles().size()).isEqualTo(8); // checking source
    // files
    String[] fileNames = {
      "Globals.pas",
      "MainWindow.pas",
      "OverloadTest.pas",
      "StatementTest.pas",
      "CommentsTest.pas",
      "AccessorsTest.Pas",
      "FunctionTest.pas",
      "GlobalsTest.pas"
    };
    for (int i = 0; i < fileNames.length; ++i) {
      assertThat(project.getSourceFiles().get(i).getName()).isEqualTo(fileNames[i]);
    }

    assertThat(project.getIncludeDirectories().size()).isEqualTo(2); // checking
    // include
    // directories
    String[] includeNames = {"includes1", "includes2"};
    for (int i = 0; i < includeNames.length; ++i) {
      assertThat(project.getIncludeDirectories().get(i).getName()).isEqualTo(includeNames[i]);
    }

    assertThat(project.getDefinitions().size()).isEqualTo(4);
    String[] definitionNames = {"GGMSGDEBUGx", "LOGTOFILEx", "FullDebugMode", "RELEASE"};
    for (int i = 0; i < definitionNames.length; ++i) {
      assertThat(project.getDefinitions().get(i)).isEqualTo(definitionNames[i]);
    }
  }
}
