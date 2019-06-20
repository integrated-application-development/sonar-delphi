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
import java.nio.charset.Charset;
import java.util.Collections;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.rule.ActiveRules;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.delphi.DelphiTestUtils;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.pmd.profile.DelphiPmdProfileExporter;
import org.sonar.plugins.delphi.project.DelphiProject;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiPmdSensorTest {

  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/PMDTest";
  private static final String TEST_FILE = "/org/sonar/plugins/delphi/PMDTest/pmd.pas";

  private DelphiPmdSensor sensor;
  private SensorContextTester sensorContext;
  private DelphiProjectHelper delphiProjectHelper;
  private DelphiPmdProfileExporter profileExporter;
  private ActiveRules rulesProfile;
  private File baseDir;

  @Before
  public void init() throws IOException {
    baseDir = DelphiUtils.getResource(ROOT_NAME);
    sensorContext = SensorContextTester.create(baseDir);

    delphiProjectHelper = DelphiTestUtils.mockProjectHelper();

    // Don't pollute current working directory
    when(delphiProjectHelper.workDir()).thenReturn(new File("target"));

    File srcFile = DelphiUtils.getResource(TEST_FILE);

    final InputFile inputFile = TestInputFileBuilder
        .create("ROOT_KEY_CHANGE_AT_SONARAPI_5", baseDir, srcFile)
        .setModuleBaseDir(baseDir.toPath())
        .setLanguage(DelphiLanguage.KEY)
        .setType(InputFile.Type.MAIN)
        .setContents(DelphiUtils.readFileContent(srcFile, Charset.defaultCharset().name()))
        .build();

    DelphiProject delphiProject = new DelphiProject("Default Project");
    delphiProject.setSourceFiles(Collections.singletonList(srcFile));

    when(delphiProjectHelper.getProjects())
        .thenReturn(Collections.singletonList(delphiProject));

    when(delphiProjectHelper.getFile(anyString())).thenAnswer(
        (Answer<InputFile>) invocation -> inputFile);

    rulesProfile = mock(ActiveRules.class);
    profileExporter = mock(DelphiPmdProfileExporter.class);

    String fileName = getClass().getResource("/org/sonar/plugins/delphi/pmd/rules.xml").getPath();
    File rulesFile = new File(fileName);
    String rulesXmlContent;
    try {
      rulesXmlContent = FileUtils.readFileToString(rulesFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    sensor = new DelphiPmdSensor(delphiProjectHelper, sensorContext, rulesProfile);
  }

}
