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
package org.sonar.plugins.delphi.codecoverage.delphicodecoveragetool;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.DefaultInputFile;
import org.sonar.plugins.delphi.DelphiTestUtils;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.debug.DebugSensorContext;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.when;

public class DelphiCoverageToolParserTest
{
  private DebugSensorContext context;
  private File baseDir;
  private DelphiProjectHelper delphiProjectHelper;

  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/SimpleDelphiProject";
  private static final String REPORT_FILE = "/org/sonar/plugins/delphi/SimpleDelphiProject/reports/Coverage.xml";

  private final File reportFile = DelphiUtils.getResource(REPORT_FILE);

  @Before
  public void init() throws FileNotFoundException {

    context = new DebugSensorContext();

    baseDir = DelphiUtils.getResource(ROOT_NAME);

    List<File> sourceDirs = new ArrayList<File>();

    sourceDirs.add(baseDir); // include baseDir

    delphiProjectHelper = DelphiTestUtils.mockProjectHelper();
    InputFile inputFile = new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5",reportFile.getPath()).setModuleBaseDir(Paths.get(ROOT_NAME));
    when(delphiProjectHelper.findFileInDirectories(REPORT_FILE)).thenReturn(inputFile);

    when(delphiProjectHelper.findFileInDirectories(anyString())).thenAnswer(new Answer<InputFile>() {
      @Override
      public InputFile answer(InvocationOnMock invocation) throws Throwable {
        InputFile inputFile = new DefaultInputFile("ROOT_KEY_CHANGE_AT_SONARAPI_5",((String) invocation.getArguments()[0])).setModuleBaseDir(Paths.get(ROOT_NAME));

        return inputFile;
      }
    });
  }

  @Test
  public void parseTest() {
    DelphiCodeCoverageToolParser parser = new DelphiCodeCoverageToolParser(reportFile, delphiProjectHelper);
    parser.parse(context);

    String coverage_names[] = {"Globals.pas:coverage", "MainWindow.pas:coverage"};
    double coverage_values[] = {100.00, 50.00};
    String lineHits_names[] = {"Globals.pas:coverage_line_hits_data", "MainWindow.pas:coverage_line_hits_data"};
    String lineHits_values[] = {"19=1;20=1", "36=1;37=0;38=1;39=0"};

    for (int i = 0; i < coverage_names.length; ++i) { // % of coverage
      assertEquals(coverage_names[i] + "-coverage", coverage_values[i], context.getMeasure(coverage_names[i])
        .getValue(), 0.0);
      assertEquals(coverage_names[i] + "-lineHits", lineHits_values[i], context.getMeasure(lineHits_names[i])
        .getData());
    }
  }

}
