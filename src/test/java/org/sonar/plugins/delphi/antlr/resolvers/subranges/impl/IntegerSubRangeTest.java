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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.plugins.delphi.antlr.resolvers.subranges.SubRange;

public class IntegerSubRangeTest {

  private SubRange range;
  private SubRange range2;

  @Rule public ExpectedException exceptionCatcher = ExpectedException.none();

  @Before
  public void setup() {
    range = new IntegerSubRange(0, 5);
    range2 = new IntegerSubRange(-1, -1);
  }

  @Test
  public void testGetBegin() {
    assertEquals(0, range.getBegin());
    assertEquals(-1, range2.getBegin());

    range.setBegin(2);
    assertEquals(2, range.getBegin());

    range2.setBegin(-2);
    assertEquals(-2, range2.getBegin());
  }

  @Test
  public void testGetEnd() {
    assertEquals(5, range.getEnd());
    assertEquals(-1, range2.getEnd());

    range.setEnd(2);
    assertEquals(2, range.getEnd());

    range2.setEnd(0);
    assertEquals(0, range2.getEnd());
  }

  @Test
  public void testValueInRange() {
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
  public void testRangeInRange() {
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
  public void testInvalidStartEndShouldThrow() {
    exceptionCatcher.expect(IllegalArgumentException.class);
    new IntegerSubRange(1, 0);
  }
}
