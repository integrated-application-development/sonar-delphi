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
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.fs.internal.FileMetadata;
import org.sonar.api.batch.fs.internal.Metadata;
import org.sonar.api.batch.fs.internal.TestInputFileBuilder;
import org.sonar.api.batch.sensor.internal.SensorContextTester;
import org.sonar.plugins.delphi.core.DelphiLanguage;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DelphiCoverageToolParserTest
{
  private SensorContextTester context;
  private File baseDir;
  private DelphiProjectHelper delphiProjectHelper;

  private static final String ROOT_NAME = "/org/sonar/plugins/delphi/SimpleDelphiProject";
  private static final String REPORT_FILE = "/org/sonar/plugins/delphi/SimpleDelphiProject/reports/Coverage.xml";

  private final File reportFile = DelphiUtils.getResource(REPORT_FILE);

  private void addFile(String fileName) throws FileNotFoundException
  {
    File file = DelphiUtils.getResource(fileName);
    InputStream fileStream = new FileInputStream(file);
    Metadata metadata = new FileMetadata().readMetadata(fileStream, StandardCharsets.UTF_8, file.getPath());
    final InputFile inputFile = TestInputFileBuilder.create("", baseDir, file)
        .setLanguage(DelphiLanguage.KEY)
        .setMetadata(metadata)
        .build();
    context.fileSystem().add(inputFile);
  }

  @Before
  public void init() throws FileNotFoundException {

    baseDir = DelphiUtils.getResource(ROOT_NAME);

    context = SensorContextTester.create(baseDir);

    List<File> sourceDirs = new ArrayList<>();

    sourceDirs.add(baseDir); // include baseDir

    delphiProjectHelper = new DelphiProjectHelper(context.config(), context.fileSystem());

    addFile(ROOT_NAME + "/Globals.pas");
    addFile(ROOT_NAME + "/MainWindow.pas");
  }

  @Test
  public void parseTest() {
    DelphiCodeCoverageToolParser parser = new DelphiCodeCoverageToolParser(reportFile, delphiProjectHelper);
    parser.parse(context);

    assertEquals((Integer)1, context.lineHits(":Globals.pas", 16));
    assertEquals((Integer)1, context.lineHits(":Globals.pas", 17));
    assertEquals((Integer)0, context.lineHits(":Globals.pas", 23));

    assertEquals((Integer)1, context.lineHits(":MainWindow.pas", 31));
    assertEquals((Integer)1, context.lineHits(":MainWindow.pas", 36));
    assertEquals((Integer)1, context.lineHits(":MainWindow.pas", 37));
    assertEquals((Integer)1, context.lineHits(":MainWindow.pas", 38));
    assertEquals((Integer)1, context.lineHits(":MainWindow.pas", 39));
    assertEquals((Integer)1, context.lineHits(":MainWindow.pas", 40));
  }

}
