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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Aggregates sub ranges
 */
public class SubRangeAggregator {

  protected ArrayList<SubRange> data = new ArrayList<SubRange>();

  /**
   * add a new sub range
   * 
   * @param newRange new sub range
   */
  public void add(SubRange newRange) {
    if (newRange == null) {
      return;
    }

    for (SubRange range : data) {
      if (range.inRange(newRange)) {
        return;
      }
    }

    data.add(newRange);
  }

  /**
   * check if providen value is in range of all agregated ranges
   * 
   * @param value value to check
   * @return true if so, false otherwise
   */
  public boolean inRange(int value) {
    for (SubRange range : data) {
      if (range.inRange(value)) {
        return true;
      }
    }
    return false;
  }

  /**
   * adds all elements from another aggregator, check for duplications and
   * merges them
   * 
   * @param subRangeAggregator another aggregator
   */
  public void addAll(SubRangeAggregator subRangeAggregator) {
    for (SubRange newRange : subRangeAggregator.getRanges()) {
      add(newRange);
    }
  }

  /**
   * adds all elements from another aggregator, check for duplications and
   * merges them
   * 
   * @param subRangeAggregator another aggregator
   */
  public void addAll(SubRange... subRange) {
    for (SubRange newRange : subRange) {
      add(newRange);
    }
  }

  /**
   * sort aggregated sub ranges with providen comparator
   * 
   * @param comparator Sub range comparator
   */
  public void sort(Comparator<? super SubRange> comparator) {
    Collections.sort(data, comparator);
  }

  /**
   * @return get the list of all aggregated sub ranges
   */
  public List<SubRange> getRanges() {
    return data;
  }
}
