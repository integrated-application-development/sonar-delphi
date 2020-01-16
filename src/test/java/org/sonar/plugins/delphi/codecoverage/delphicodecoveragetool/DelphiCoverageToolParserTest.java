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

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DelphiCoverageToolParserTest {

  private SensorContextTester context;
  private File baseDir;
  private DelphiProjectHelper delphiProjectHelper;

  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/projects/SimpleProject";
  private static final String REPORT_FILE =
      "/org/sonar/plugins/delphi/projects/SimpleProject/reports/Coverage.xml";

  private final File reportFile = DelphiUtils.getResource(REPORT_FILE);

  private void addFile(String fileName) throws IOException {
    File file = DelphiUtils.getResource(fileName);
    final InputFile inputFile =
        TestInputFileBuilder.create("", baseDir, file)
            .setLanguage(DelphiLanguage.KEY)
            .setContents(DelphiUtils.readFileContent(file, delphiProjectHelper.encoding()))
            .build();
    context.fileSystem().add(inputFile);
  }

  @Before
  public void init() throws IOException {

    baseDir = DelphiUtils.getResource(ROOT_NAME);

    context = SensorContextTester.create(baseDir);

    delphiProjectHelper = new DelphiProjectHelper(context.config(), context.fileSystem());

    addFile(ROOT_NAME + "/Globals.pas");
    addFile(ROOT_NAME + "/MainWindow.pas");
  }

  @Test
  public void testParse() {
    DelphiCodeCoverageToolParser parser =
        new DelphiCodeCoverageToolParser(reportFile, delphiProjectHelper);
    parser.parse(context);

    assertThat(context.lineHits(":Globals.pas", 16)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(":Globals.pas", 17)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(":Globals.pas", 23)).isEqualTo((Integer) 0);

    assertThat(context.lineHits(":MainWindow.pas", 31)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(":MainWindow.pas", 36)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(":MainWindow.pas", 37)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(":MainWindow.pas", 38)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(":MainWindow.pas", 39)).isEqualTo((Integer) 1);
    assertThat(context.lineHits(":MainWindow.pas", 40)).isEqualTo((Integer) 1);
  }
}
