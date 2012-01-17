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
package org.sonar.plugins.delphi.pmd;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.resources.Project;
import org.sonar.api.resources.ProjectFileSystem;
import org.sonar.api.rules.Violation;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.debug.DebugRuleFinder;
import org.sonar.plugins.delphi.debug.DebugSensorContext;
import org.sonar.plugins.delphi.utils.DelphiUtils;

class RuleData {

  private String name;
  private int line;

  public RuleData(String _name, int _line) {
    name = _name;
    line = _line;
  }

  public String getName() {
    return name;
  }

  public int getLine() {
    return line;
  }

  public static Comparator<RuleData> getComparator() {
    return new Comparator<RuleData>() {

      @Override
      public int compare(RuleData o1, RuleData o2) {
        return o1.getLine() - o2.getLine();
      }
    };
  }
}

public class DelphiPmdSensorTest {

  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/PMDTest";
  private static final String TEST_FILE = "/org/sonar/plugins/delphi/PMDTest/pmd.pas";

  private Project project;
  private DelphiPmdSensor sensor;

  @Before
  public void init() {
    project = mock(Project.class);
    ProjectFileSystem pfs = mock(ProjectFileSystem.class);

    File baseDir = DelphiUtils.getResource(ROOT_NAME);

    when(project.getLanguage()).thenReturn(new DelphiLanguage());
    when(project.getFileSystem()).thenReturn(pfs);
    when(project.getLanguageKey()).thenReturn(DelphiLanguage.KEY);

    when(pfs.getBasedir()).thenReturn(baseDir);

    File srcFile = DelphiUtils.getResource(TEST_FILE);
    List<File> sourceFiles = new ArrayList<File>();
    sourceFiles.add(srcFile);

    when(pfs.getSourceFiles(DelphiLanguage.instance)).thenReturn(sourceFiles);

    sensor = new DelphiPmdSensor(new DebugRuleFinder());
  }

  @Test
  public void shouldExecuteOnProjectTest() {
    assertTrue(sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void analyseTest() {
    DebugSensorContext debugContext = new DebugSensorContext();
    sensor.analyse(project, debugContext);

    RuleData ruleData[] = // all expected rule violations and their lines
    { new RuleData("Class Name Rule", 7), new RuleData("No Semi After Overload Rule", 9), new RuleData("Public Fields Rule", 10),
        new RuleData("Type Alias Rule", 13), new RuleData("Type Alias Rule", 14), new RuleData("One Class Per File Rule", 19),
        new RuleData("Empty Interface Rule", 25), new RuleData("Interface Name Rule", 25), new RuleData("No Guid Rule", 29),
        new RuleData("Record Name Rule", 34), new RuleData("Inherited Method With No Code Rule", 45), new RuleData("Then Try Rule", 51),
        new RuleData("Empty Except Block Rule", 54), new RuleData("Too Many Arguments Rule", 58), new RuleData("Too Long Method Rule", 58),
        new RuleData("Too Many Variables Rule", 59), new RuleData("Uppercase Reserved Keywords Rule", 63),
        new RuleData("No Function Return Type Rule", 97), new RuleData("Avoid Out Parameter Rule", 98),
        new RuleData("Catching General Exception Rule", 103), new RuleData("Empty Begin Statement Rule", 104),
        new RuleData("If True Rule", 109), new RuleData("If True Rule", 110), new RuleData("Raising General Exception Rule", 111),
        new RuleData("If Not False Rule", 113), new RuleData("Unused Arguments Rule", 117), new RuleData("Assigned And Free Rule", 125),
        new RuleData("Assigned And Free Rule", 126), new RuleData("Empty Else Statement Rule", 135),
        new RuleData("Assigned And Free Rule", 147), new RuleData("Mixed Names Rule", 163), new RuleData("Mixed Names Rule", 169),
        new RuleData("Mixed Names Rule", 175), new RuleData("Constructor Without Inherited Statement Rule", 190),
        new RuleData("Destructor Without Inherited Statement Rule", 196), new RuleData("No 'begin' after 'do' Rule", 228),
        new RuleData("With After Do/Then Rule", 248), new RuleData("No Semicolon Rule", 290), new RuleData("No Semicolon Rule", 291),
        new RuleData("No Semicolon Rule", 294), new RuleData("Too Long Method Rule", 243), new RuleData("With After Do/Then Rule", 262),
        new RuleData("Cast And Free Rule", 302), new RuleData("Cast And Free Rule", 303) };

    Arrays.sort(ruleData, RuleData.getComparator()); // we don't have to add violations in line order, so we sort them
    assertEquals("Number of found violations don't match", ruleData.length, debugContext.getViolationsCount());

    for (int i = 0; i < debugContext.getViolationsCount(); ++i) {
      Violation violation = debugContext.getViolation(i); // violation
      assertEquals(ruleData[i].getName(), violation.getRule().getName()); // rule name
      assertEquals("LINE AT " + ruleData[i].getName(), ruleData[i].getLine(), violation.getLineId().intValue()); // rule line
      // System.out.println((i+1) + ": " + violation.getRule().getName() + ", line " + violation.getLineId() );
    }
  }

}
