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

import static org.junit.Assert.*;

public class IntegerSubRangeTest {

  private SubRange range;
  private SubRange range2;

  @Before
  public void setup() {
    range = new IntegerSubRange(0, 5);
    range2 = new IntegerSubRange(-1, -1);
  }

  @Test
  public void getBeginTest() {
    assertEquals(0, range.getBegin());
    assertEquals(-1, range2.getBegin());

    range.setBegin(2);
    assertEquals(2, range.getBegin());

    range2.setBegin(-2);
    assertEquals(-2, range2.getBegin());
  }

  @Test
  public void getEndTest() {
    assertEquals(5, range.getEnd());
    assertEquals(-1, range2.getEnd());

    range.setEnd(2);
    assertEquals(2, range.getEnd());

    range2.setEnd(0);
    assertEquals(0, range2.getEnd());
  }

  @Test
  public void valueInRangeTest() {
    assertTrue(range.inRange(0));
    assertTrue(range.inRange(3));
    assertTrue(range.inRange(5));
    assertFalse(range.inRange(-1));
    assertFalse(range.inRange(6));

    assertTrue(range2.inRange(-1));
    assertFalse(range2.inRange(-2));
    assertFalse(range2.inRange(0));
  }

  @Test
  public void rangeInRangeTest() {
    assertTrue(range.inRange(range));
    assertTrue(range.inRange(new IntegerSubRange(2, 4)));
    assertTrue(range.inRange(new IntegerSubRange(0, 0)));
    assertTrue(range.inRange(new IntegerSubRange(5, 5)));
    assertTrue(range.inRange(new IntegerSubRange(1, 5)));
    assertTrue(range.inRange(new IntegerSubRange(4, 5)));
    assertTrue(range.inRange(new IntegerSubRange(5, 5)));

    assertFalse(range.inRange(new IntegerSubRange(-5, 10)));
    assertFalse(range.inRange(new IntegerSubRange(0, 6)));
    assertFalse(range.inRange(new IntegerSubRange(-1, 0)));
    assertFalse(range.inRange(new IntegerSubRange(5, 6)));
    assertFalse(range.inRange(new IntegerSubRange(-1, 5)));
  }

  @Test
  public void equalsTest() {
    assertTrue(new IntegerSubRange(0, 1).equals(new IntegerSubRange(0, 1)));
    assertTrue(new IntegerSubRange(0, 10).equals(new IntegerSubRange(0, 10)));
    assertTrue(new IntegerSubRange(0, 1).equals(new StringSubRange(0, 1, null)));
    assertFalse(new IntegerSubRange(0, 1).equals(new IntegerSubRange(1, 1)));
  }

}
