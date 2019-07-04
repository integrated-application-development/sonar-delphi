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

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.plugins.delphi.DelphiTestUtils;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public abstract class BasePmdRuleTest {

  protected static final String ROOT_DIR_NAME = "/org/sonar/plugins/delphi/PMDTest";
  protected static final File ROOT_DIR = DelphiUtils.getResource(ROOT_DIR_NAME);

  private SensorContextTester sensorContext;
  private DelphiProjectHelper delphiProjectHelper;

  protected DelphiPmdSensor sensor;
  protected List<Issue> issues = new ArrayList<>();
  private File testFile;
  private ActiveRules rulesProfile;
  private File baseDir;

  public void execute(DelphiUnitBuilderTest builder) {
    configureTest(builder);

    SensorContextTester sensorContext = SensorContextTester.create(baseDir);
    sensor.execute(sensorContext);
    //todo: REACTIVATE THIS TEST, ATM PROBLEMS WITH TREE TESTLOGIC
    //assertThat("Errors: " + sensor.getErrors(), sensor.getErrors(), is(empty()));
  }

  private void configureTest(DelphiUnitBuilderTest builder) {
    testFile = builder.buildFile(ROOT_DIR);

    String relativePathTestFile = DelphiUtils
        .getRelativePath(testFile, Collections.singletonList(ROOT_DIR));

    configureTest(ROOT_DIR_NAME + "/" + relativePathTestFile, builder);
  }

  protected void configureTest(String testFileName, DelphiUnitBuilderTest builder) {
    sensorContext = SensorContextTester.create(ROOT_DIR);
    delphiProjectHelper = DelphiTestUtils.mockProjectHelper();

    // Don't pollute current working directory
    when(delphiProjectHelper.workDir()).thenReturn(new File("target"));

    File srcFile = DelphiUtils.getResource(testFileName);

    baseDir = DelphiUtils.getResource(ROOT_DIR_NAME);

    InputFile inputFile = TestInputFileBuilder
        .create("ROOT_KEY_CHANGE_AT_SONARAPI_5", baseDir, srcFile)
        .setModuleBaseDir(baseDir.toPath())
        .setContents(builder.getSourceCode().toString())
        .build();

    sensorContext.fileSystem().add(inputFile);

    DelphiProject delphiProject = new DelphiProject("Default Project");
    delphiProject.setSourceFiles(Collections.singletonList(srcFile));

    when(delphiProjectHelper.getProjects())
        .thenReturn(Collections.singletonList(delphiProject));
    when(delphiProjectHelper.getFile(anyString())).thenAnswer(new Answer<InputFile>() {
      @Override
      public InputFile answer(InvocationOnMock invocation) {
        InputFile inputFile = TestInputFileBuilder.create("ROOT_KEY_CHANGE_AT_SONARAPI_5",
            Paths.get(ROOT_DIR_NAME).toFile(),
              new File((String) invocation.getArguments()[0])).build();

        return inputFile;
      }
    });

    rulesProfile = mock(ActiveRules.class);

    String fileName = getClass().getResource("/org/sonar/plugins/delphi/pmd/rules.xml").getPath();
    File rulesFile = new File(fileName);
    String rulesXmlContent;
    try {
      rulesXmlContent = FileUtils.readFileToString(rulesFile, StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    sensor = new DelphiPmdSensor(delphiProjectHelper, sensorContext, rulesProfile);
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
    return "Issue [ruleKey=" + issue.ruleKey() + ", message=" + issue.primaryLocation().message()
        + ", line=" + issue.primaryLocation().textRange().start().line() + "]";
  }

}
