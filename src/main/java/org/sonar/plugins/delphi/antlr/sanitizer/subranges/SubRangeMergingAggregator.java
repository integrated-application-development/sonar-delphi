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

/**
 * Aggregates sub ranges, merges them if neccessary to reduce workload
 */
public class SubRangeMergingAggregator extends SubRangeAggregator {

  /**
   * {@inheritDoc}
   */

  @Override
  public void add(SubRange newRange) {
    if (newRange == null) {
      return;
    }

    for (SubRange range : data) {
      if (range.inRange(newRange)) {
        return;
      }

      if (shouldMerge(range, newRange)) {
        range = mergeRanges(range, newRange);
        removeDuplications(range);
        return;
      }
    }
    data.add(newRange);
  }

  protected boolean shouldMerge(SubRange range1, SubRange range2) {
    if (range1.inRange(range2)) {
      return true;
    }
    if (range2.inRange(range1)) {
      return true;
    }
    if (range1.inRange(range2.getBegin())) {
      return true;
    }
      return range2.inRange(range1.getBegin());
  }

  protected SubRange mergeRanges(SubRange rangeInList, SubRange newRange) {
    int begin = Math.min(rangeInList.getBegin(), newRange.getBegin());
    int end = Math.max(rangeInList.getEnd(), newRange.getEnd());

    rangeInList.setBegin(begin);
    rangeInList.setEnd(end);

    return rangeInList;
  }

  protected void removeDuplications(SubRange rangeToCheck) {
    for (int i = 0; i < data.size(); ++i) {
      SubRange atRange = data.get(i);
      if (atRange.equals(rangeToCheck)) {
        continue;
      }
      if (rangeToCheck.inRange(atRange)) {
        data.remove(i);
      }
    }
  }

}
