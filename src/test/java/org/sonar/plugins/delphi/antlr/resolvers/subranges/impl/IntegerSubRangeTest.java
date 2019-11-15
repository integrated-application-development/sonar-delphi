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
    assertThat(range.getBegin()).isEqualTo(0);
    assertThat(range2.getBegin()).isEqualTo(-1);

    range.setBegin(2);
    assertThat(range.getBegin()).isEqualTo(2);

    range2.setBegin(-2);
    assertThat(range2.getBegin()).isEqualTo(-2);
  }

  @Test
  public void testGetEnd() {
    assertThat(range.getEnd()).isEqualTo(5);
    assertThat(range2.getEnd()).isEqualTo(-1);

    range.setEnd(2);
    assertThat(range.getEnd()).isEqualTo(2);

    range2.setEnd(0);
    assertThat(range2.getEnd()).isEqualTo(0);
  }

  @Test
  public void testValueInRange() {
    assertThat(range.inRange(0)).isTrue();
    assertThat(range.inRange(3)).isTrue();
    assertThat(range.inRange(5)).isTrue();
    assertThat(range.inRange(-1)).isFalse();
    assertThat(range.inRange(6)).isFalse();

    assertThat(range2.inRange(-1)).isTrue();
    assertThat(range2.inRange(-2)).isFalse();
    assertThat(range2.inRange(0)).isFalse();
  }

  @Test
  public void testRangeInRange() {
    assertThat(range.inRange(range)).isTrue();
    assertThat(range.inRange(new IntegerSubRange(2, 4))).isTrue();
    assertThat(range.inRange(new IntegerSubRange(0, 0))).isTrue();
    assertThat(range.inRange(new IntegerSubRange(5, 5))).isTrue();
    assertThat(range.inRange(new IntegerSubRange(1, 5))).isTrue();
    assertThat(range.inRange(new IntegerSubRange(4, 5))).isTrue();
    assertThat(range.inRange(new IntegerSubRange(5, 5))).isTrue();

    assertThat(range.inRange(new IntegerSubRange(-5, 10))).isFalse();
    assertThat(range.inRange(new IntegerSubRange(0, 6))).isFalse();
    assertThat(range.inRange(new IntegerSubRange(-1, 0))).isFalse();
    assertThat(range.inRange(new IntegerSubRange(5, 6))).isFalse();
    assertThat(range.inRange(new IntegerSubRange(-1, 5))).isFalse();
  }

  @Test
  public void testInvalidStartEndShouldThrow() {
    exceptionCatcher.expect(IllegalArgumentException.class);
    new IntegerSubRange(1, 0);
  }
}
