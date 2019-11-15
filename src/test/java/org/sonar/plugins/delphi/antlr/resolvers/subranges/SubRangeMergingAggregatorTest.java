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

public class SubRangeMergingAggregatorTest {

  private SubRangeMergingAggregator aggregator;

  @Before
  public void setup() {
    aggregator = new SubRangeMergingAggregator();
  }

  @Test
  public void testShouldMerge() {
    assertThat(aggregator.shouldMerge(new IntegerSubRange(0, 5), new IntegerSubRange(5, 10)))
        .isTrue();
    assertThat(aggregator.shouldMerge(new IntegerSubRange(5, 10), new IntegerSubRange(0, 5)))
        .isTrue();

    assertThat(aggregator.shouldMerge(new IntegerSubRange(0, 10), new IntegerSubRange(4, 5)))
        .isTrue();
    assertThat(aggregator.shouldMerge(new IntegerSubRange(4, 5), new IntegerSubRange(0, 10)))
        .isTrue();

    assertThat(aggregator.shouldMerge(new IntegerSubRange(5, 10), new IntegerSubRange(11, 11)))
        .isFalse();
    assertThat(aggregator.shouldMerge(new IntegerSubRange(11, 11), new IntegerSubRange(5, 10)))
        .isFalse();

    assertThat(aggregator.shouldMerge(new IntegerSubRange(0, 1), new IntegerSubRange(2, 3)))
        .isFalse();
    assertThat(aggregator.shouldMerge(new IntegerSubRange(2, 3), new IntegerSubRange(0, 1)))
        .isFalse();
  }

  @Test
  public void testMerge() {
    SubRange result = aggregator.mergeRanges(new IntegerSubRange(0, 5), new IntegerSubRange(5, 10));
    assertThat(result.getBegin()).isEqualTo(0);
    assertThat(result.getEnd()).isEqualTo(10);

    result = aggregator.mergeRanges(new IntegerSubRange(5, 10), new IntegerSubRange(5, 10));
    assertThat(result.getBegin()).isEqualTo(5);
    assertThat(result.getEnd()).isEqualTo(10);

    result = aggregator.mergeRanges(new IntegerSubRange(-5, 5), new IntegerSubRange(2, 5));
    assertThat(result.getBegin()).isEqualTo(-5);
    assertThat(result.getEnd()).isEqualTo(5);

    result = aggregator.mergeRanges(new IntegerSubRange(0, 2), new IntegerSubRange(1, 1));
    assertThat(result.getBegin()).isEqualTo(0);
    assertThat(result.getEnd()).isEqualTo(2);
  }

  @Test
  public void testAddAll() {
    SubRangeAggregator aggregator2 = new SubRangeMergingAggregator();
    aggregator.add(new IntegerSubRange(0, 10));
    assertThat(aggregator.getRanges().size()).isEqualTo(1);

    aggregator2.add(new IntegerSubRange(1, 2));
    aggregator2.add(new IntegerSubRange(3, 5));
    aggregator2.add(new IntegerSubRange(6, 11));
    assertThat(aggregator2.getRanges().size()).isEqualTo(3);

    aggregator.addAll(aggregator2); // should not add repeating ranges and
    // merge other
    assertThat(aggregator.getRanges().size()).isEqualTo(1);
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
  public void testAdd() {
    assertThat(aggregator.getRanges().size()).isEqualTo(0);

    aggregator.add(new IntegerSubRange(0, 5)); // should add
    assertThat(aggregator.getRanges().size()).isEqualTo(1);

    aggregator.add(new IntegerSubRange(6, 7)); // should add
    assertThat(aggregator.getRanges().size()).isEqualTo(2);

    aggregator.add(new IntegerSubRange(5, 10)); // should merge
    assertThat(aggregator.getRanges().size()).isEqualTo(1);

    aggregator.add(new IntegerSubRange(2, 7)); // shouldn't be added
    assertThat(aggregator.getRanges().size()).isEqualTo(1);

    aggregator.add(new IntegerSubRange(-1, -1)); // should add
    assertThat(aggregator.getRanges().size()).isEqualTo(2);

    aggregator.add(new IntegerSubRange(-2, -2)); // should add
    assertThat(aggregator.getRanges().size()).isEqualTo(3);

    aggregator.add(new IntegerSubRange(-2, -1)); // should merge
    assertThat(aggregator.getRanges().size()).isEqualTo(2);

    aggregator.add(new IntegerSubRange(-100, 100)); // should merge
    assertThat(aggregator.getRanges().size()).isEqualTo(1);
  }
}
