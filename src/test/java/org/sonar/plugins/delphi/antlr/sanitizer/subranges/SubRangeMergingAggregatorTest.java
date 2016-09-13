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
package org.sonar.plugins.delphi.antlr.sanitizer.subranges;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.impl.IntegerSubRange;

import java.util.Arrays;

import static org.junit.Assert.*;

public class SubRangeMergingAggregatorTest {

  private SubRangeMergingAggregator aggregator;

  @Before
  public void setup() {
    aggregator = new SubRangeMergingAggregator();
  }

  @Test
  public void shouldMergeTest() {
    assertTrue(aggregator.shouldMerge(new IntegerSubRange(0, 5), new IntegerSubRange(5, 10)));
    assertTrue(aggregator.shouldMerge(new IntegerSubRange(5, 10), new IntegerSubRange(0, 5)));

    assertTrue(aggregator.shouldMerge(new IntegerSubRange(0, 10), new IntegerSubRange(4, 5)));
    assertTrue(aggregator.shouldMerge(new IntegerSubRange(4, 5), new IntegerSubRange(0, 10)));

    assertFalse(aggregator.shouldMerge(new IntegerSubRange(5, 10), new IntegerSubRange(11, 11)));
    assertFalse(aggregator.shouldMerge(new IntegerSubRange(11, 11), new IntegerSubRange(5, 10)));

    assertFalse(aggregator.shouldMerge(new IntegerSubRange(0, 1), new IntegerSubRange(2, 3)));
    assertFalse(aggregator.shouldMerge(new IntegerSubRange(2, 3), new IntegerSubRange(0, 1)));
  }

  @Test
  public void mergeTest() {
    SubRange result = aggregator.mergeRanges(new IntegerSubRange(0, 5), new IntegerSubRange(5, 10));
    assertEquals(0, result.getBegin());
    assertEquals(10, result.getEnd());

    result = aggregator.mergeRanges(new IntegerSubRange(5, 10), new IntegerSubRange(5, 10));
    assertEquals(5, result.getBegin());
    assertEquals(10, result.getEnd());

    result = aggregator.mergeRanges(new IntegerSubRange(-5, 5), new IntegerSubRange(2, 5));
    assertEquals(-5, result.getBegin());
    assertEquals(5, result.getEnd());

    result = aggregator.mergeRanges(new IntegerSubRange(0, 2), new IntegerSubRange(1, 1));
    assertEquals(0, result.getBegin());
    assertEquals(2, result.getEnd());
  }

  @Test
  public void addAllTest() {
    SubRangeAggregator aggregator2 = new SubRangeMergingAggregator();
    aggregator.add(new IntegerSubRange(0, 10));
    assertEquals(1, aggregator.getRanges().size());

    aggregator2.add(new IntegerSubRange(1, 2));
    aggregator2.add(new IntegerSubRange(3, 5));
    aggregator2.add(new IntegerSubRange(6, 11));
    assertEquals(3, aggregator2.getRanges().size());

    aggregator.addAll(aggregator2); // should not add repeating ranges and
                                    // merge other
    assertEquals(1, aggregator.getRanges().size());
  }

  @Test
  public void sortTest() {
    SubRange data[] = {new IntegerSubRange(0, 10), new IntegerSubRange(-5, -1), new IntegerSubRange(12, 12),
      new IntegerSubRange(15, 19),
      new IntegerSubRange(-10, -6)};

    for (SubRange range : data) {
      aggregator.add(range);
    }

    Arrays.sort(data, new SubRangeFirstOccurenceComparator());
    aggregator.sort(new SubRangeFirstOccurenceComparator());

    int index = 0;
    for (SubRange sortedRange : aggregator.getRanges()) {
      assertEquals(data[index++], sortedRange);
    }

  }

  @Test
  public void addTest() {
    assertEquals(0, aggregator.getRanges().size());

    aggregator.add(new IntegerSubRange(0, 5)); // should add
    assertEquals(1, aggregator.getRanges().size());

    aggregator.add(new IntegerSubRange(6, 7)); // should add
    assertEquals(2, aggregator.getRanges().size());

    aggregator.add(new IntegerSubRange(5, 10)); // should merge
    assertEquals(1, aggregator.getRanges().size());

    aggregator.add(new IntegerSubRange(2, 7)); // shouldn't be added
    assertEquals(1, aggregator.getRanges().size());

    aggregator.add(new IntegerSubRange(-1, -1)); // should add
    assertEquals(2, aggregator.getRanges().size());

    aggregator.add(new IntegerSubRange(-2, -2)); // should add
    assertEquals(3, aggregator.getRanges().size());

    aggregator.add(new IntegerSubRange(-2, -1)); // should merge
    assertEquals(2, aggregator.getRanges().size());

    aggregator.add(new IntegerSubRange(-100, 100)); // should merge
    assertEquals(1, aggregator.getRanges().size());
  }

}
