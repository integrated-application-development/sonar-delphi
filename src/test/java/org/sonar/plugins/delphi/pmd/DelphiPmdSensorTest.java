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
package org.sonar.plugins.delphi.pmd;

import java.io.File;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.issue.Issue;
import org.sonar.api.resources.Project;
import org.sonar.plugins.delphi.DelphiTestUtils;
import org.sonar.plugins.delphi.StubIssueBuilder;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.debug.DebugSensorContext;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

public class DelphiPmdSensorTest {

  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/PMDTest";
  private static final String TEST_FILE = "/org/sonar/plugins/delphi/PMDTest/pmd.pas";

  private Project project;
  private DelphiPmdSensor sensor;
  private ResourcePerspectives perspectives;
  private DelphiProjectHelper delphiProjectHelper;
  private Issuable issuable;
  private List<Issue> issues = new LinkedList<Issue>();

  @Before
  public void init() {
    project = mock(Project.class);
    perspectives = mock(ResourcePerspectives.class);
    delphiProjectHelper = DelphiTestUtils.mockProjectHelper();

    // Don't pollute current working directory
    when(delphiProjectHelper.workDir()).thenReturn(new File("target"));

    File srcFile = DelphiUtils.getResource(TEST_FILE);

    InputFile inputFile = new DefaultInputFile(ROOT_NAME)
      .setFile(srcFile);

    DelphiProject delphiProject = new DelphiProject("Default Project");
    delphiProject.setSourceFiles(Arrays.asList(inputFile));

    issuable = mock(Issuable.class);

    when(delphiProjectHelper.getWorkgroupProjects()).thenReturn(Arrays.asList(delphiProject));
    when(delphiProjectHelper.getFile(anyString())).thenAnswer(new Answer<InputFile>() {
      @Override
      public InputFile answer(InvocationOnMock invocation) throws Throwable {
        InputFile inputFile = new DefaultInputFile(ROOT_NAME).setFile(new File((String) invocation
          .getArguments()[0]));

        when(perspectives.as(Issuable.class, inputFile)).thenReturn(issuable);

        when(issuable.newIssueBuilder()).thenReturn(new StubIssueBuilder());

        return inputFile;
      }
    });

    when(issuable.addIssue(Matchers.any(Issue.class))).then(new Answer<Boolean>() {
      @Override
      public Boolean answer(InvocationOnMock invocation) throws Throwable {
        Issue issue = (Issue) invocation.getArguments()[0];
        issues.add(issue);
        return Boolean.TRUE;
      }
    });

    sensor = new DelphiPmdSensor(delphiProjectHelper, perspectives);
  }

  @Test
  public void shouldExecuteOnProjectTest() {
    assertTrue(sensor.shouldExecuteOnProject(project));
  }

  @Test
  public void analyseTest() {
    // TODO Create one test per violation

    DebugSensorContext debugContext = new DebugSensorContext();
    sensor.analyse(project, debugContext);

    RuleData ruleData[] = // all expected rule violations and their lines
    {new RuleData("Class Name Rule", 7),
      new RuleData("No Semi After Overload Rule", 9),
      new RuleData("Public Fields Rule", 10),
      new RuleData("Type Alias Rule", 13),
      new RuleData("Type Alias Rule", 14),
      new RuleData("One Class Per File Rule", 19),
      new RuleData("Empty Interface Rule", 25),
      new RuleData("Interface Name Rule", 25),
      new RuleData("No Guid Rule", 29),
      new RuleData("Record Name Rule", 34),
      new RuleData("Inherited Method With No Code Rule", 45),
      new RuleData("Empty Except Block Rule", 54),
      new RuleData("Too Many Arguments Rule", 58),
      new RuleData("Too Many Variables Rule", 59),
      new RuleData("Uppercase Reserved Keywords Rule", 63),
      new RuleData("Avoid Out Parameter Rule", 98),
      new RuleData("Catching General Exception Rule", 103),
      new RuleData("Empty Begin Statement Rule", 104),
      new RuleData("If True Rule", 109),
      new RuleData("If True Rule", 110),
      new RuleData("Raising General Exception Rule", 111),
      new RuleData("If Not False Rule", 113),
      new RuleData("Unused Arguments Rule", 117),
      new RuleData("Assigned And Free Rule", 125),
      new RuleData("Assigned And Free Rule", 126),
      new RuleData("Empty Else Statement Rule", 135),
      new RuleData("Assigned And Free Rule", 147),
      new RuleData("Mixed Names Rule", 163),
      new RuleData("Mixed Names Rule", 169),
      new RuleData("Mixed Names Rule", 175),
      new RuleData("Constructor Without Inherited Statement Rule", 190),
      new RuleData("Destructor Without Inherited Statement Rule", 196),
      new RuleData("No 'begin' after 'do' Rule", 228),
      new RuleData("With After Do/Then Rule", 248),
      new RuleData("No Semicolon Rule", 289),
      new RuleData("No Semicolon Rule", 291),
      new RuleData("No Semicolon Rule", 293),
      new RuleData("With After Do/Then Rule", 262),
      new RuleData("Cast And Free Rule", 302),
      new RuleData("Cast And Free Rule", 303)};

    // Sort the violations by line number, so we don't have to add
    // violations order
    Arrays.sort(ruleData, RuleData.getComparator());

    assertThat("number of issues", issues, hasSize(40));

    for (int i = 0; i < issues.size(); ++i) {
      Issue issue = issues.get(i);

      System.out.println(issue.ruleKey().rule() + ":" + issue.line());

      assertThat(ruleData[i].toString(), issue.ruleKey().rule(), is(ruleData[i].getName()));
      assertThat(ruleData[i].toString(), issue.line(), is(ruleData[i].getLine()));
    }
  }
}
