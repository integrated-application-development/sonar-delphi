/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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
package org.sonar.plugins.delphi.antlr.sanitizer.resolvers;

import org.sonar.plugins.delphi.antlr.sanitizer.SourceResolver;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRangeAggregator;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRangeFirstOccurenceComparator;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRangeMergingAggregator;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.impl.IntegerSubRange;

/**
 * Parses file to look for places to be excluded from processing, such as: strings and comments.
 */
public class ExcludeResolver extends SourceResolver {

  @Override
  protected void doResolve(SourceResolverResults results) {
    results.setFileExcludes(getAllExcludes(results.getFileData()));
  }

  /**
   * @return sub range aggregator containing all exclude elements: comments, strings
   */
  private SubRangeAggregator getAllExcludes(StringBuilder fileData) {
    SubRangeMergingAggregator rangeAggregator = new SubRangeMergingAggregator();

    if (fileData != null) {
      rangeAggregator.addAll(excludeComments(fileData));
      rangeAggregator.addAll(excludeBlockComments(fileData));
      rangeAggregator.addAll(getExcludedStrings(fileData));
    }
    rangeAggregator.sort(new SubRangeFirstOccurenceComparator());
    return rangeAggregator;
  }

  private SubRangeAggregator excludeComments(StringBuilder fileData) {
    SubRangeAggregator rangeAggregator = new SubRangeMergingAggregator();
    int pos = -1; // //...
    while ((pos = fileData.indexOf("//", pos + 1)) != -1) {
      int pos2 = fileData.indexOf("\n", pos + 1); // EOL
      if (pos2 != -1 && !rangeAggregator.inRange(pos)) {
        rangeAggregator.add(new IntegerSubRange(pos, pos2)); // make new subrange
      }
    }
    return rangeAggregator;
  }

  private SubRangeAggregator excludeBlockComments(StringBuilder fileData) {
    SubRangeAggregator rangeAggregator = new SubRangeMergingAggregator();
    int pos = -1; // (* ... *)
    while ((pos = fileData.indexOf("(*", pos + 1)) != -1) {
      int pos2 = fileData.indexOf("*)", pos + 1);
      if (pos2 != -1 && !rangeAggregator.inRange(pos)) {
        rangeAggregator.add(new IntegerSubRange(pos, pos2 + 2)); // make new subrange
      }
    }

    pos = -1; // { ... }
    while ((pos = fileData.indexOf("{", pos + 1)) != -1) {
      if (fileData.charAt(pos + 1) == '$') {
        continue;
      }
      int pos2 = fileData.indexOf("}", pos + 1);
      if (pos2 != -1 && !rangeAggregator.inRange(pos)) {
        rangeAggregator.add(new IntegerSubRange(pos, pos2 + 1)); // make new subrange
      }
    }
    return rangeAggregator;
  }

  private SubRangeAggregator getExcludedStrings(StringBuilder fileData) {
    SubRangeAggregator rangeAggregator = new SubRangeMergingAggregator();
    int pos = -1;
    while ((pos = fileData.indexOf("'", pos + 1)) != -1) // parses strings
    {
      int pos2 = fileData.indexOf("'", pos + 1); // get next ' position
      if (pos2 == -1) {
        break; // no pair
      }
      int newLine = fileData.indexOf("\n", pos + 1); // get new line position
      if (pos2 > newLine) {
        continue; // count only those quotes, that begin and end in a single line
      }
      rangeAggregator.add(new IntegerSubRange(pos, pos2 + 1)); // new quote range
      pos = pos2; // increase pos to get next '' pair
    }
    return rangeAggregator;
  }

}
