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
package org.sonar.plugins.delphi.utils;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;

public class DelphiUtilsTest {

  @Test
  public void getAbsolutePathTest() throws IOException
  {
    File tempFile = File.createTempFile("testFile", "tmp");
    tempFile.deleteOnExit();

    String rootPath = tempFile.getParent();

    String str1 = DelphiUtils.resolveAbsolutePath(rootPath, tempFile.getName()).getAbsolutePath();
    String str2 = DelphiUtils.resolveAbsolutePath(rootPath, "tempDir").getAbsolutePath();
    String str3 = DelphiUtils.resolveAbsolutePath(rootPath, rootPath).getAbsolutePath();

    assertEquals(rootPath + File.separatorChar + tempFile.getName(), str1);
    assertEquals(rootPath + File.separatorChar + "tempDir", str2);
    assertEquals(rootPath, str3);
  }

  @Test
  public void checkRangeTest() {
    assertEquals(75.00, DelphiUtils.checkRange(75.00, 75.00, 75.00), 0.0);
    assertEquals(100.00, DelphiUtils.checkRange(75.00, 100.00, 150.00), 0.0);
    assertEquals(50.00, DelphiUtils.checkRange(75.00, 00.00, 50.00), 0.0);
  }

  @Test
  public void acceptFile() {
    assertThat(DelphiUtils.acceptFile("Unit.pas"), is(true));
    assertThat(DelphiUtils.acceptFile("Project.dpr"), is(true));
    assertThat(DelphiUtils.acceptFile("Package.dpk"), is(true));
  }

  @Test
  public void acceptFileCaseInsensitive() {
    assertThat(DelphiUtils.acceptFile("Unit.Pas"), is(true));
    assertThat(DelphiUtils.acceptFile("Project.dPr"), is(true));
    assertThat(DelphiUtils.acceptFile("Package.DPK"), is(true));
  }

}
