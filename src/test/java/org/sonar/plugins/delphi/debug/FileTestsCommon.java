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
package org.sonar.plugins.delphi.debug;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.sonar.plugins.delphi.utils.DelphiUtils;

public class FileTestsCommon {

  protected static File testFile = null;
  protected static StringBuilder testFileString = null;

  protected static void loadFile(String fileName) throws IOException {
    testFile = DelphiUtils.getResource(fileName);
    testFileString = new StringBuilder();

    String line;
    BufferedReader reader = new BufferedReader(new FileReader(testFile));
    while ((line = reader.readLine()) != null) {
      testFileString.append(line);
      testFileString.append('\n');
    }
    reader.close();
  }

}
