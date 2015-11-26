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
package org.sonar.plugins.delphi.antlr.sanitizer.resolvers;

import java.util.List;
import java.util.Set;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirective;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirectiveFactory;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirectiveType;
import org.sonar.plugins.delphi.antlr.directives.exceptions.CompilerDirectiveFactorySyntaxException;
import org.sonar.plugins.delphi.antlr.directives.impl.IfDefDirective;
import org.sonar.plugins.delphi.antlr.sanitizer.SourceResolver;
import org.sonar.plugins.delphi.antlr.sanitizer.resolvers.exceptions.DefineResolverException;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRange;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRangeAggregator;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRangeFirstOccurenceComparator;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.SubRangeMergingAggregator;
import org.sonar.plugins.delphi.antlr.sanitizer.subranges.impl.IntegerSubRange;
import org.sonar.plugins.delphi.utils.DelphiUtils;

/**
 * Resolves defines in a given file, cuts out the unwanted definitions
 */
public class DefineResolver extends SourceResolver {

  private Set<String> definitions;

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
    StringBuilder newData = resolveDefines(results.getFileData(), results.getFileExcludes(), definitions);
    results.setFileData(newData);
  }

  /**
   * Resolve defines
   * 
   * @param str File data
   * @param excluded Excluded areas, not to analyze
   * @return New file data with parsed preprocesor defines
   * @throws DefineResolverException when no matching {$ifdef} .. {$endif}
   *             directives will be found
   */
  private StringBuilder resolveDefines(StringBuilder str, SubRangeAggregator excluded, Set<String> defines) {
    if (str == null) {
      return null;
    }

    try {
      CompilerDirectiveFactory factory = new CompilerDirectiveFactory();
      List<CompilerDirective> allDirectives = factory.produce(str.toString());
      SubRangeAggregator toComment = processCompilerDirectives(allDirectives, defines, excluded);
      commentUnwantedDefinitions(str, toComment);
    } catch (CompilerDirectiveFactorySyntaxException e) {
      DelphiUtils.LOG.debug(e.getMessage());
    } catch (DefineResolverException e) {
      DelphiUtils.LOG.debug(e.getMessage());
    }

    return str;
  }

  private SubRangeAggregator processCompilerDirectives(List<CompilerDirective> directives, Set<String> defines,
    SubRangeAggregator excluded)
    throws DefineResolverException {
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
        toDelete.addAll(getMatchingEndIfCutRange(directives, i, excluded, true));
      } else if (type == CompilerDirectiveType.IFDEF) {
        boolean isDefined = defines.contains(directive.getItem());
        boolean isPositive = ((IfDefDirective) directive).isPositive();
        boolean shouldCut = (isDefined != isPositive);
        toDelete.addAll(getMatchingEndIfCutRange(directives, i, excluded, shouldCut));
      }
    }

    return toDelete;
  }

  private SubRange[] getMatchingEndIfCutRange(List<CompilerDirective> directives, int startDirectiveIndex,
    SubRangeAggregator excluded,
    boolean shouldCut) throws DefineResolverException {
    CompilerDirective firstDirective = directives.get(startDirectiveIndex);
    CompilerDirective lastDirective = null;
    CompilerDirective elseDirective = null;

    int index = startDirectiveIndex;

    // how many $ifdef..$endif we must skip
    int branchCount = 1;

    while (branchCount > 0) {

      if (++index >= directives.size()) {
        throw new DefineResolverException("No matching {$ifdef}...{$endif} pair found for {" + firstDirective + "}");
      }

      lastDirective = directives.get(index);
      if (excluded.inRange(lastDirective.getFirstCharPosition())) {
        // if in excluded range, continue
        continue;
      }

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

  private SubRange[] calculateCutSubRange(CompilerDirective firstDirective, CompilerDirective lastDirective,
    CompilerDirective elseDirective,
    boolean shouldCut) {
    // starting position to cut
    int cutStart = firstDirective.getFirstCharPosition();
    // end position to cut
    int cutEnd = -1;

    SubRange subRange2 = null;

    // statement not defined, cut not matching code
    if (shouldCut) {
      if (elseDirective == null) {
        // cut to $endif
        cutEnd = lastDirective.getLastCharPosition() + 1;
      } else {
        // cut to $else
        cutEnd = elseDirective.getLastCharPosition() + 1;
        subRange2 = new IntegerSubRange(lastDirective.getFirstCharPosition(), lastDirective.getLastCharPosition() + 1);
      }
    } else {
      // statement defined, but need to cut else if present
      if (elseDirective != null) {
        // start with $else
        cutStart = elseDirective.getFirstCharPosition();
        cutEnd = lastDirective.getLastCharPosition() + 1;
        subRange2 = new IntegerSubRange(firstDirective.getFirstCharPosition(), firstDirective.getLastCharPosition() + 1);
      } else {
        cutEnd = firstDirective.getLastCharPosition() + 1;
        subRange2 = new IntegerSubRange(lastDirective.getFirstCharPosition(), lastDirective.getLastCharPosition() + 1);
      }
    }

    if (cutEnd != -1 && cutStart != -1) {
      if (subRange2 != null) {
        return new SubRange[] {new IntegerSubRange(cutStart, cutEnd), subRange2};
      }
      return new SubRange[] {new IntegerSubRange(cutStart, cutEnd)};
    }

    return null;
  }

  private void commentUnwantedDefinitions(StringBuilder str, SubRangeAggregator toComment) {
    int insertedChars = 0;
    toComment.sort(new SubRangeFirstOccurenceComparator());
    for (SubRange range : toComment.getRanges()) {

      final String strRange = str.substring(range.getBegin() + insertedChars, range.getEnd() + insertedChars);

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
