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
package org.sonar.plugins.delphi.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DelphiUtilsTest {

  @Test
  public void getAbsolutePathTest() {
    String root = "C:\\test";

    String str1 = DelphiUtils.resolveAbsolutePath(root, "branch").getAbsolutePath();
    String str2 = DelphiUtils.resolveAbsolutePath(root, "C:\\test\\directory").getAbsolutePath();
    String str3 = DelphiUtils.resolveAbsolutePath(root, "branch\\tools").getAbsolutePath();

    assertEquals("C:\\test\\branch", str1);
    assertEquals("C:\\test\\directory", str2);
    assertEquals("C:\\test\\branch\\tools", str3);
  }

  @Test
  public void checkRangeTest() {
    assertEquals(75.00, DelphiUtils.checkRange(75.00, 75.00, 75.00), 0.0);
    assertEquals(100.00, DelphiUtils.checkRange(75.00, 100.00, 150.00), 0.0);
    assertEquals(50.00, DelphiUtils.checkRange(75.00, 00.00, 50.00), 0.0);
  }

}
