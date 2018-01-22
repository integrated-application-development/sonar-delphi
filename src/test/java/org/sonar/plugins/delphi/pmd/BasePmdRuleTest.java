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
import org.junit.After;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.api.profiles.RulesProfile;
import org.sonar.plugins.delphi.DelphiTestUtils;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdProfileExporter;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class BasePmdRuleTest {

  protected static final String ROOT_DIR_NAME = "/org/sonar/plugins/delphi/PMDTest";
  protected static final File ROOT_DIR = DelphiUtils.getResource(ROOT_DIR_NAME);

  private SensorContextTester sensorContext;
  private DelphiProjectHelper delphiProjectHelper;

  protected DelphiPmdSensor sensor;
  protected List<Issue> issues = new LinkedList<Issue>();
  private File testFile;
  private DelphiPmdProfileExporter profileExporter;
  private RulesProfile rulesProfile;
  private File baseDir;

  public void execute(DelphiUnitBuilderTest builder) {
    configureTest(builder);

    baseDir = DelphiUtils.getResource(ROOT_DIR_NAME);

    SensorContextTester sensorContext = SensorContextTester.create(baseDir);
    sensor.execute(sensorContext);
    //todo: REACTIVATE THIS TEST, ATM PROBLEMS WITH TREE TESTLOGIC
    //assertThat("Errors: " + sensor.getErrors(), sensor.getErrors(), is(empty()));
  }

  private void configureTest(DelphiUnitBuilderTest builder) {
    testFile = builder.buildFile(ROOT_DIR);

    String relativePathTestFile = DelphiUtils.getRelativePath(testFile, Arrays.asList(ROOT_DIR));

    configureTest(ROOT_DIR_NAME + "/" + relativePathTestFile);
  }

  protected void configureTest(String testFileName) {
    sensorContext = SensorContextTester.create(ROOT_DIR);
    delphiProjectHelper = DelphiTestUtils.mockProjectHelper();

    // Don't pollute current working directory
    when(delphiProjectHelper.workDir()).thenReturn(new File("target"));

    File srcFile = DelphiUtils.getResource(testFileName);

    InputFile inputFile = new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5",srcFile.getPath()).setModuleBaseDir(Paths.get(ROOT_DIR_NAME));

    DelphiProject delphiProject = new DelphiProject("Default Project");
    delphiProject.setSourceFiles(Arrays.asList(inputFile));

    when(delphiProjectHelper.getWorkgroupProjects()).thenReturn(Arrays.asList(delphiProject));
    when(delphiProjectHelper.getFile(anyString())).thenAnswer(new Answer<InputFile>() {
      @Override
      public InputFile answer(InvocationOnMock invocation) throws Throwable {
        InputFile inputFile = new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5",(new File((String) invocation
                .getArguments()[0])).getPath()).setModuleBaseDir(Paths.get(ROOT_DIR_NAME));

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

  @After
  public void teardown() {
    if (testFile != null) {
      testFile.delete();
    }
  }

  public String toString(List<Issue> issues) {
    StringBuilder builder = new StringBuilder();
    builder.append("[");
    for (Issue issue : issues) {
      builder.append(toString(issue)).append(',');
    }
    builder.append("]");
    return builder.toString();
  }

  public String toString(Issue issue) {
    return "Issue [ruleKey=" + issue.ruleKey() + ", message=" + issue.primaryLocation().message() + ", line=" + issue.primaryLocation().textRange().start().line() + "]";
  }

}
