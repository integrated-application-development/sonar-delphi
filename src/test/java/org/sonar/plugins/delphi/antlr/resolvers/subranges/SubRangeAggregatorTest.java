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
package org.sonar.plugins.delphi.antlr.resolvers.subranges;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.resolvers.subranges.impl.IntegerSubRange;

public class SubRangeAggregatorTest {

  private SubRangeAggregator aggregator;

  @Before
  public void setup() {
    aggregator = new SubRangeAggregator();
  }

  @Test
  public void testSort() {
    SubRange[] data = {
      new IntegerSubRange(0, 10),
      new IntegerSubRange(-5, -1),
      new IntegerSubRange(12, 12),
      new IntegerSubRange(15, 19),
      new IntegerSubRange(-10, -6)
    };

    for (SubRange range : data) {
      aggregator.add(range);
    }

    Arrays.sort(data, new SubRangeFirstOccurrenceComparator());
    aggregator.sort(new SubRangeFirstOccurrenceComparator());

    int index = 0;
    for (SubRange sortedRange : aggregator.getRanges()) {
      assertThat(sortedRange).isEqualTo(data[index++]);
    }
  }

  @Test
  public void testAddTest() {
    assertThat(aggregator.getRanges().size()).isEqualTo(0);

    aggregator.add(new IntegerSubRange(0, 5)); // should add
    assertThat(aggregator.getRanges().size()).isEqualTo(1);

    aggregator.add(new IntegerSubRange(6, 7)); // should add
    assertThat(aggregator.getRanges().size()).isEqualTo(2);

    aggregator.add(new IntegerSubRange(5, 10)); // should add
    assertThat(aggregator.getRanges().size()).isEqualTo(3);

    aggregator.add(new IntegerSubRange(0, 10)); // should add
    assertThat(aggregator.getRanges().size()).isEqualTo(4);

    aggregator.add(new IntegerSubRange(2, 3)); // should not add
    assertThat(aggregator.getRanges().size()).isEqualTo(4);
  }
}
