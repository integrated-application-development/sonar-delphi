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
import java.util.HashSet;
import java.util.Set;
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
    project = DelphiProject.create("simple project");
  }

  @Test
  public void testSimpleProject() throws IOException {
    File sourceFile = File.createTempFile("tempfile", ".pas");
    sourceFile.deleteOnExit();

    assertThat(project.getName()).isEqualTo("simple project");
    assertThat(project.getConditionalDefines()).isEmpty();
    assertThat(project.getSearchDirectories()).isEmpty();
    assertThat(project.getSourceFiles()).isEmpty();

    project.addDefinition("DEF");
    assertThat(project.getConditionalDefines()).hasSize(1);
    project.addUnitScopeName("System");
    assertThat(project.getUnitScopeNames()).hasSize(1);
    project.addSourceFile(DelphiUtils.getResource(XML_FILE).toPath());
    assertThat(project.getSourceFiles()).isEmpty();
    project.addSourceFile(sourceFile.toPath());
    assertThat(project.getSourceFiles()).hasSize(1);
    project.addSearchDirectory(DelphiUtils.getResource(INC_DIR).toPath());
    assertThat(project.getSearchDirectories()).hasSize(1);
  }

  @Test
  public void testSetDefinitions() {
    Set<String> defs = new HashSet<>();
    project.setDefinitions(defs);
    assertThat(project.getConditionalDefines()).isEqualTo(defs);
  }

  @Test
  public void testParseFile() {
    project = DelphiProject.parse(DelphiUtils.getResource(XML_FILE).toPath());

    assertThat(project.getName()).isEqualTo("Simple Delphi Project");
    assertThat(project.getSourceFiles()).hasSize(8);

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
      assertThat(project.getSourceFiles().get(i)).hasFileName(fileNames[i]);
    }

    assertThat(project.getSearchDirectories()).hasSize(2);

    String[] includeNames = {"includes1", "includes2"};
    for (int i = 0; i < includeNames.length; ++i) {
      assertThat(project.getSearchDirectories().get(i).getFileName()).hasToString(includeNames[i]);
    }

    assertThat(project.getConditionalDefines())
        .containsOnly(
            "MSWINDOWS", "CPUX86", "GGMSGDEBUGx", "LOGTOFILEx", "FullDebugMode", "RELEASE");

    assertThat(project.getUnitScopeNames()).containsOnly("Vcl", "System");
  }
}
