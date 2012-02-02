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
   * @param definitions
   *          existing definitions in a file
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
   * @param str
   *          File data
   * @param excluded
   *          Excluded areas, not to analyze
   * @return New file data with parsed preprocesor defines
   * @throws DefineResolverException
   *           when no matching {$ifdef} .. {$endif} directives will be found
   */
  private StringBuilder resolveDefines(StringBuilder str, SubRangeAggregator excluded, Set<String> defines) {
    if (str == null) {
      return null;
    }

    try {
      CompilerDirectiveFactory factory = new CompilerDirectiveFactory();
      List<CompilerDirective> allDirectives = factory.produce(str.toString());
      SubRangeAggregator toDelete = processCompilerDirectives(allDirectives, defines, excluded);
      removeUnwantedDefinitions(str, toDelete);
    } catch (CompilerDirectiveFactorySyntaxException e) {
      DelphiUtils.LOG.debug(e.getMessage());
    } catch (DefineResolverException e) {
      DelphiUtils.LOG.debug(e.getMessage());
    }

    return str;
  }

  private SubRangeAggregator processCompilerDirectives(List<CompilerDirective> directives, Set<String> defines, SubRangeAggregator excluded)
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
        toDelete.add(getMatchingEndIfCutRange(directives, i, excluded, true)); // mark places to cut off
      } else if (type == CompilerDirectiveType.IFDEF) {
        boolean isDefined = defines.contains(directive.getItem());
        boolean isPositive = ((IfDefDirective) directive).isPositive();
        boolean shouldCut = (isDefined == isPositive);
        toDelete.add(getMatchingEndIfCutRange(directives, i, excluded, shouldCut));
      }
    }

    return toDelete;
  }

  private SubRange getMatchingEndIfCutRange(List<CompilerDirective> directives, int startDirectiveIndex, SubRangeAggregator excluded,
      boolean shouldCut) throws DefineResolverException {
    CompilerDirective firstDirective = directives.get(startDirectiveIndex);
    CompilerDirective lastDirective = null;
    CompilerDirective elseDirective = null;

    int index = startDirectiveIndex;
    int branchCount = 1; // how many $ifdef..$endif we must skip

    while (branchCount > 0) {

      if (++index >= directives.size()) {
        throw new DefineResolverException("No matching {$ifdef}...{$endif} pair found for {" + firstDirective + "}");
      }

      lastDirective = directives.get(index);
      if (excluded.inRange(lastDirective.getFirstCharPosition())) {
        continue; // if in excluded range, continue
      }

      CompilerDirectiveType type = lastDirective.getType();
      if (type == CompilerDirectiveType.IFDEF) {
        ++branchCount;
      } else if (type == CompilerDirectiveType.ENDIF) {
        --branchCount;
      } else if (type == CompilerDirectiveType.ELSE && branchCount == 1) {
        elseDirective = lastDirective;
      }
    }

    return calculateCutSubRange(firstDirective, lastDirective, elseDirective, shouldCut);
  }

  private SubRange calculateCutSubRange(CompilerDirective firstDirective, CompilerDirective lastDirective, CompilerDirective elseDirective,
      boolean shouldCut) {
    int cutStart = firstDirective.getFirstCharPosition(); // starting position to cut
    int cutEnd = -1; // end position to cut
    if (shouldCut) // statement not defined, cut not matching code
    {
      if (elseDirective == null) {
        cutEnd = lastDirective.getLastCharPosition() + 1; // cut to $endif
      } else {
        cutEnd = elseDirective.getLastCharPosition() + 1; // cut to $else
      }
    } else { // statement defined, but need to cut else if present
      if (elseDirective != null) {
        cutStart = elseDirective.getFirstCharPosition(); // start with $else
      }
      cutEnd = lastDirective.getLastCharPosition() + 1; // cut to $endif
    }

    if (cutEnd != -1 && cutStart != -1) {
      return new IntegerSubRange(cutStart, cutEnd);
    }

    return null;
  }

  private void removeUnwantedDefinitions(StringBuilder str, SubRangeAggregator toDelete) {
    int deleted = 0; // number of deleted chars
    toDelete.sort(new SubRangeFirstOccurenceComparator());// sort the list
    for (SubRange range : toDelete.getRanges()) { // cut the code
      str.delete(range.getBegin() - deleted, range.getEnd() - deleted);
      deleted += range.getEnd() - range.getBegin();
    }
  }

}
