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
    {new RuleData("ClassNameRule", 7),
      new RuleData("NoSemiAfterOverloadRule", 9),
      new RuleData("PublicFieldsRule", 10),
      new RuleData("TypeAliasRule", 13),
      new RuleData("TypeAliasRule", 14),
      new RuleData("OneClassPerFileRule", 19),
      new RuleData("EmptyInterfaceRule", 25),
      new RuleData("InterfaceNameRule", 25),
      new RuleData("NoGuidRule", 29),
      new RuleData("RecordNameRule", 34),
      new RuleData("InheritedMethodWithNoCodeRule", 45),
      new RuleData("ThenTryRule", 51),
      new RuleData("EmptyExceptBlockRule", 54),
      new RuleData("TooManyArgumentsRule", 58),
      new RuleData("TooManyVariablesRule", 59),
      new RuleData("UppercaseReservedKeywordsRule", 63),
      new RuleData("NoFunctionReturnTypeRule", 97),
      new RuleData("AvoidOutParameterRule", 98),
      new RuleData("CatchingGeneralExceptionRule", 103),
      new RuleData("EmptyBeginStatementRule", 104),
      new RuleData("IfTrueRule", 109),
      new RuleData("IfTrueRule", 110),
      new RuleData("RaisingGeneralExceptionRule", 111),
      new RuleData("IfNotFalseRule", 113),
      new RuleData("UnusedArgumentsRule", 117),
      new RuleData("AssignedAndFreeRule", 125),
      new RuleData("AssignedAndFreeRule", 126),
      new RuleData("EmptyElseStatementRule", 135),
      new RuleData("AssignedAndFreeRule", 147),
      new RuleData("EmptyBeginStatementRule", 158),
      new RuleData("MixedNamesRule", 163),
      new RuleData("MixedNamesRule", 169),
      new RuleData("MixedNamesRule", 175),
      new RuleData("ConstructorWithoutInheritedStatementRule", 190),
      new RuleData("DestructorWithoutInheritedStatementRule", 196),
      new RuleData("NoBeginAfterDoRule", 228),
      new RuleData("WithAfterDoThenRule", 248),
      new RuleData("NoSemicolonRule", 289),
      new RuleData("NoSemicolonRule", 291),
      new RuleData("NoSemicolonRule", 293),
      new RuleData("WithAfterDoThenRule", 262),
      new RuleData("CastAndFreeRule", 302),
      new RuleData("CastAndFreeRule", 303)};

    // Sort the violations by line number, so we don't have to add
    // violations order
    Arrays.sort(ruleData, RuleData.getComparator());

    assertThat("number of issues", issues, hasSize(ruleData.length));

    for (int i = 0; i < issues.size(); ++i) {
      Issue issue = issues.get(i);

      System.out.println(issue.ruleKey().rule() + ":" + issue.line());

      assertThat(ruleData[i].toString(), issue.ruleKey().rule(), is(ruleData[i].getName()));
      assertThat(ruleData[i].toString(), issue.line(), is(ruleData[i].getLine()));
    }
  }
}
