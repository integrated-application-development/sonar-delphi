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
package org.sonar.plugins.delphi.antlr.resolvers.subranges.impl;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.resolvers.subranges.SubRange;

public class StringSubRangeTest {

  private SubRange range;

  @Before
  public void setup() {
    String str = "!This is a test string!";
    range = new StringSubRange(0, str.length(), str);
  }

  @Test
  public void testToString() {
    assertThat(range.toString()).isEqualTo("[0, 23] !This is a test string!");

    range.setBegin(0);
    range.setEnd(10);
    assertThat(range.toString()).isEqualTo("[0, 10] !This is a");

    range.setEnd(15);
    range.setBegin(11);
    assertThat(range.toString()).isEqualTo("[11, 15] test");
  }
}
