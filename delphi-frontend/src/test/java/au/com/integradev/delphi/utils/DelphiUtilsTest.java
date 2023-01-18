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
package au.com.integradev.delphi.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class DelphiUtilsTest {

  @Test
  void testGetAbsolutePath() throws IOException {
    File tempFile = File.createTempFile("testFile", "tmp");
    tempFile.deleteOnExit();

    String rootPath = tempFile.getParent();

    String str1 = DelphiUtils.resolveAbsolutePath(rootPath, tempFile.getName()).getAbsolutePath();
    String str2 = DelphiUtils.resolveAbsolutePath(rootPath, "tempDir").getAbsolutePath();
    String str3 = DelphiUtils.resolveAbsolutePath(rootPath, rootPath).getAbsolutePath();

    assertThat(str1).isEqualTo(rootPath + File.separatorChar + tempFile.getName());
    assertThat(str2).isEqualTo(rootPath + File.separatorChar + "tempDir");
    assertThat(str3).isEqualTo(rootPath);
  }

  @Test
  void testAcceptFile() {
    assertThat(DelphiUtils.acceptFile("Unit.pas")).isTrue();
    assertThat(DelphiUtils.acceptFile("Project.dpr")).isTrue();
    assertThat(DelphiUtils.acceptFile("Package.dpk")).isTrue();
  }

  @Test
  void testAcceptFileCaseInsensitive() {
    assertThat(DelphiUtils.acceptFile("Unit.Pas")).isTrue();
    assertThat(DelphiUtils.acceptFile("Project.dPr")).isTrue();
    assertThat(DelphiUtils.acceptFile("Package.DPK")).isTrue();
  }
}
