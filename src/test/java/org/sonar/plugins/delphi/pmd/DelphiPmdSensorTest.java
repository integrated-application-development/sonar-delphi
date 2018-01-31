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

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.plugins.delphi.DelphiTestUtils;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdProfileExporter;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.api.batch.sensor.internal.SensorContextTester;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DelphiPmdSensorTest {

  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/PMDTest";
  private static final String TEST_FILE = "/org/sonar/plugins/delphi/PMDTest/pmd.pas";

  private DelphiPmdSensor sensor;
  private SensorContextTester sensorContext;
  private DelphiProjectHelper delphiProjectHelper;
  private DelphiPmdProfileExporter profileExporter;
  private RulesProfile rulesProfile;
  private File baseDir;
  private InputFile inputFile;

  private String getRelativePath(File prefix, String fullPath)
  {
    String result = fullPath.substring(prefix.getAbsolutePath().length() + 1);
    return result;
  }

  @Before
  public void init() {
    baseDir = DelphiUtils.getResource(ROOT_NAME);
    sensorContext = SensorContextTester.create(baseDir);

    delphiProjectHelper = DelphiTestUtils.mockProjectHelper();

    // Don't pollute current working directory
    when(delphiProjectHelper.workDir()).thenReturn(new File("target"));

    File srcFile = DelphiUtils.getResource(TEST_FILE);

    inputFile = new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5",getRelativePath(baseDir,srcFile.getPath()))
        .setModuleBaseDir(baseDir.toPath())
        .setLanguage(DelphiLanguage.KEY)
        .setType(InputFile.Type.MAIN)
        .initMetadata(new FileMetadata().readMetadata(srcFile, Charset.defaultCharset()));

    DelphiProject delphiProject = new DelphiProject("Default Project");
    delphiProject.setSourceFiles(Collections.singletonList(inputFile));

    when(delphiProjectHelper.getWorkgroupProjects()).thenReturn(Collections.singletonList(delphiProject));

    when(delphiProjectHelper.getFile(anyString())).thenAnswer(new Answer<InputFile>() {
      @Override
      public InputFile answer(InvocationOnMock invocation) {
        return inputFile;
      }
    });

    rulesProfile = mock(RulesProfile.class);
    profileExporter = mock(DelphiPmdProfileExporter.class);

    String fileName = getClass().getResource("/org/sonar/plugins/delphi/pmd/rules.xml").getPath();
    File rulesFile = new File(fileName);
    String rulesXmlContent;
    try {
      rulesXmlContent = FileUtils.readFileToString(rulesFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    when(profileExporter.exportProfileToString(rulesProfile)).thenReturn(rulesXmlContent);

    sensor = new DelphiPmdSensor(delphiProjectHelper, sensorContext, rulesProfile, profileExporter);
  }

  @Test
  public void analyseTest() {
    // TODO Create one test per violation
    sensor.execute(sensorContext);

    RuleData ruleData[] = // all expected rule violations and their lines
    {new RuleData("ClassNameRule", 7),
      new RuleData("NoSemiAfterOverloadRule", 9),
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
      new RuleData("WithAfterDoThenRule", 262),
      new RuleData("NoSemicolonRule", 289),
      new RuleData("NoSemicolonRule", 291),
      new RuleData("NoSemicolonRule", 293),
      new RuleData("CastAndFreeRule", 302),
      new RuleData("CastAndFreeRule", 303)};

    // Sort the violations by line number, so we don't have to add
    // violations order
    Arrays.sort(ruleData, RuleData.getComparator());

    org.sonar.api.batch.sensor.issue.Issue[] issues = sensorContext.allIssues().toArray(new org.sonar.api.batch.sensor.issue.Issue[0]);
    assertEquals("number of issues", ruleData.length, issues.length);

    for (int i = 0; i < issues.length; ++i) {
      Issue issue = issues[i];

      System.out.println(issue.ruleKey().rule() + ":" + issue.primaryLocation().textRange().start().line());

      assertThat(ruleData[i].toString(), issue.ruleKey().rule(), is(ruleData[i].getName()));
      assertThat(ruleData[i].toString(), issue.primaryLocation().textRange().start().line(), is(ruleData[i].getLine()));
    }
  }
}
