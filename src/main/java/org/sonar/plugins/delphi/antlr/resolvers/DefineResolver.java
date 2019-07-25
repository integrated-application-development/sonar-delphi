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
package org.sonar.plugins.delphi.antlr.resolvers;

import java.util.List;
import java.util.Set;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirective;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirectiveParser;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirectiveType;
import org.sonar.plugins.delphi.antlr.directives.exceptions.CompilerDirectiveSyntaxException;
import org.sonar.plugins.delphi.antlr.directives.impl.IfDefDirective;
import org.sonar.plugins.delphi.antlr.resolvers.exceptions.DefineResolverException;
import org.sonar.plugins.delphi.antlr.resolvers.subranges.SubRange;
import org.sonar.plugins.delphi.antlr.resolvers.subranges.SubRangeAggregator;
import org.sonar.plugins.delphi.antlr.resolvers.subranges.SubRangeFirstOccurrenceComparator;
import org.sonar.plugins.delphi.antlr.resolvers.subranges.SubRangeMergingAggregator;
import org.sonar.plugins.delphi.antlr.resolvers.subranges.impl.IntegerSubRange;

/** Resolves defines in a given file, cuts out the unwanted definitions */
public class DefineResolver extends SourceResolver {
  private static final Logger LOG = Loggers.get(DefineResolver.class);

  private final Set<String> definitions;

  /**
   * ctor
   *
   * @param definitions existing definitions in a file
   */
  public DefineResolver(Set<String> definitions) {
    this.definitions = definitions;
  }

  @Override
  protected void doResolve(SourceResolverResults results) {
    StringBuilder newData = resolveDefines(results.getFileData(), definitions);
    results.setFileData(newData);
  }

  /**
   * Resolve defines
   *
   * @param str File data
   * @return New file data with parsed preprocessor defines
   */
  private StringBuilder resolveDefines(StringBuilder str, Set<String> defines) {
    if (str == null) {
      return null;
    }

    try {
      CompilerDirectiveParser factory = new CompilerDirectiveParser();
      List<CompilerDirective> allDirectives = factory.parse(str.toString());
      SubRangeAggregator toComment = processCompilerDirectives(allDirectives, defines);
      commentUnwantedDefinitions(str, toComment);
    } catch (CompilerDirectiveSyntaxException | DefineResolverException e) {
      LOG.trace("Failed to resolve define: ", e);
    }

    return str;
  }

  private SubRangeAggregator processCompilerDirectives(
      List<CompilerDirective> directives, Set<String> defines) {
    SubRangeMergingAggregator toDelete = new SubRangeMergingAggregator();

    for (int i = 0; i < directives.size(); ++i) {

      CompilerDirective directive = directives.get(i);
      CompilerDirectiveType type = directive.getType();

      if (type == CompilerDirectiveType.DEFINE) {
        defines.add(directive.getItem());
      } else if (type == CompilerDirectiveType.UNDEFINE) {
        defines.remove(directive.getItem());
      } else if (type == CompilerDirectiveType.IF) {
        // mark places to cut off
        toDelete.addAll(getMatchingEndIfCutRange(directives, i, true));
      } else if (type == CompilerDirectiveType.IFDEF) {
        boolean isDefined = defines.contains(directive.getItem());
        boolean isPositive = ((IfDefDirective) directive).isPositive();
        boolean shouldCut = (isDefined != isPositive);
        toDelete.addAll(getMatchingEndIfCutRange(directives, i, shouldCut));
      }
    }

    return toDelete;
  }

  private SubRange[] getMatchingEndIfCutRange(
      List<CompilerDirective> directives, int startDirectiveIndex, boolean shouldCut) {
    CompilerDirective firstDirective = directives.get(startDirectiveIndex);
    CompilerDirective lastDirective = null;
    CompilerDirective elseDirective = null;

    int index = startDirectiveIndex;

    // how many $ifdef..$endif we must skip
    int branchCount = 1;

    while (branchCount > 0) {

      if (++index >= directives.size()) {
        throw new DefineResolverException(
            "No matching {$ifdef}...{$endif} pair found for {" + firstDirective + "}");
      }

      lastDirective = directives.get(index);

      CompilerDirectiveType type = lastDirective.getType();
      if (type == CompilerDirectiveType.IFDEF) {
        ++branchCount;
      } else if (type == CompilerDirectiveType.IF) {
        ++branchCount;
      } else if (type == CompilerDirectiveType.ENDIF) {
        --branchCount;
      } else if (type == CompilerDirectiveType.IFEND) {
        --branchCount;
      } else if (type == CompilerDirectiveType.ELSE && branchCount == 1) {
        elseDirective = lastDirective;
      }
    }

    return calculateCutSubRange(firstDirective, lastDirective, elseDirective, shouldCut);
  }

  private SubRange[] calculateCutSubRange(
      CompilerDirective firstDirective,
      CompilerDirective lastDirective,
      CompilerDirective elseDirective,
      boolean shouldCut) {
    // starting position to cut
    int cutStart = firstDirective.getFirstCharPosition();
    // end position to cut
    int cutEnd;

    SubRange subRange2 = null;

    // statement not defined, cut not matching code
    if (shouldCut) {
      if (elseDirective == null) {
        // cut to $endif
        cutEnd = lastDirective.getLastCharPosition() + 1;
      } else {
        // cut to $else
        cutEnd = elseDirective.getLastCharPosition() + 1;
        subRange2 =
            new IntegerSubRange(
                lastDirective.getFirstCharPosition(), lastDirective.getLastCharPosition() + 1);
      }
    } else {
      // statement defined, but need to cut else if present
      if (elseDirective != null) {
        // start with $else
        cutStart = elseDirective.getFirstCharPosition();
        cutEnd = lastDirective.getLastCharPosition() + 1;
        subRange2 =
            new IntegerSubRange(
                firstDirective.getFirstCharPosition(), firstDirective.getLastCharPosition() + 1);
      } else {
        cutEnd = firstDirective.getLastCharPosition() + 1;
        subRange2 =
            new IntegerSubRange(
                lastDirective.getFirstCharPosition(), lastDirective.getLastCharPosition() + 1);
      }
    }

    if (cutEnd != -1 && cutStart != -1) {
      if (subRange2 != null) {
        return new SubRange[] {new IntegerSubRange(cutStart, cutEnd), subRange2};
      }
      return new SubRange[] {new IntegerSubRange(cutStart, cutEnd)};
    }

    return new SubRange[0];
  }

  private void commentUnwantedDefinitions(StringBuilder str, SubRangeAggregator toComment) {
    int insertedChars = 0;
    toComment.sort(new SubRangeFirstOccurrenceComparator());
    for (SubRange range : toComment.getRanges()) {

      final String strRange =
          str.substring(range.getBegin() + insertedChars, range.getEnd() + insertedChars);

      if (strRange.contains("*)")) {
        String newRange = "(*".concat(strRange.replace("*)", " )")).concat("*)");
        str.replace(range.getBegin() + insertedChars, range.getEnd() + insertedChars, newRange);
        insertedChars += 4;
      } else {
        str.insert(range.getBegin() + insertedChars, "(*");
        insertedChars += 2;
        str.insert(range.getEnd() + insertedChars, "*)");
        insertedChars += 2;
      }
    }
  }
}
