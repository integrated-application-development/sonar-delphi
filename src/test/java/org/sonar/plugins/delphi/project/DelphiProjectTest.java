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

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DelphiProjectTest {

  private static String XML_FILE = "/org/sonar/plugins/delphi/SimpleDelphiProject/dproj/SimpleDelphiProject.dproj";
  private static String INC_DIR = "/org/sonar/plugins/delphi/SimpleDelphiProject/includes1";

  private DelphiProject project;

  @Before
  public void init() {
    project = new DelphiProject("simple project");
  }

  @Test
  public void simpleProjectTest() throws IOException {
    File sourceFile = File.createTempFile("tempfile", ".pas");
    sourceFile.deleteOnExit();

    assertEquals("simple project", project.getName());
    assertEquals(0, project.getDefinitions().size());
    assertEquals(0, project.getIncludeDirectories().size());
    assertEquals(0, project.getSourceFiles().size());
    assertEquals(null, project.getXmlFile());

    project.addDefinition("DEF");
    assertEquals(1, project.getDefinitions().size());
    project.addFile(DelphiUtils.getResource(XML_FILE).getAbsolutePath());
    assertEquals(0, project.getSourceFiles().size());
    project.addFile(sourceFile.getAbsolutePath());
    assertEquals(1, project.getSourceFiles().size());
    project.addIncludeDirectory(DelphiUtils.getResource(INC_DIR).getAbsolutePath());
    assertEquals(1, project.getIncludeDirectories().size());
  }

  @Test
  public void setDefinitionsTest() {
    List<String> defs = new ArrayList<String>();
    project.setDefinitions(defs);
    assertEquals(defs, project.getDefinitions());
  }

  @Test
  public void setFileTest() {
    File file = DelphiUtils.getResource(XML_FILE);
    project.setFile(file);
    assertEquals(file, project.getXmlFile());
  }

  @Test
  public void parseFileTest() throws IllegalArgumentException, IOException {
    project = new DelphiProject(DelphiUtils.getResource(XML_FILE));

    assertEquals("Simple Delphi Product", project.getName());

    assertEquals(8, project.getSourceFiles().size()); // checking source
                                                      // files
    String fileNames[] = {"Globals.pas", "MainWindow.pas", "OverloadTest.pas", "StatementTest.pas",
      "CommentsTest.pas",
      "AccessorsTest.Pas", "FunctionTest.pas", "GlobalsTest.pas"};
    for (int i = 0; i < fileNames.length; ++i) {
      assertEquals(fileNames[i], project.getSourceFiles().get(i).getName());
    }

    assertEquals(2, project.getIncludeDirectories().size()); // checking
                                                             // include
                                                             // directories
    String includeNames[] = {"includes1", "includes2"};
    for (int i = 0; i < includeNames.length; ++i) {
      assertEquals(includeNames[i], project.getIncludeDirectories().get(i).getName());
    }

    assertEquals(4, project.getDefinitions().size());
    String definitionNames[] = {"GGMSGDEBUGx", "LOGTOFILEx", "FullDebugMode", "RELEASE"};
    for (int i = 0; i < definitionNames.length; ++i) {
      assertEquals(definitionNames[i], project.getDefinitions().get(i));
    }
  }

}
