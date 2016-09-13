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
package org.sonar.plugins.delphi.antlr.sanitizer.subranges.impl;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRange;

import static org.junit.Assert.assertEquals;

public class StringSubRangeTest {

  private SubRange range;
  private String str;

  @Before
  public void setup() {
    str = "!This is a test string!";
    range = new StringSubRange(0, str.length(), str);
  }

  @Test
  public void toStringTest() {
    assertEquals("[0, 23] !This is a test string!", range.toString());

    range.setBegin(0);
    range.setEnd(10);
    assertEquals("[0, 10] !This is a", range.toString());

    range.setEnd(15);
    range.setBegin(11);
    assertEquals("[11, 15] test", range.toString());
  }

}
