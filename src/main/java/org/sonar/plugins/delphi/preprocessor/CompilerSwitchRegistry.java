package org.sonar.plugins.delphi.preprocessor;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;
import java.util.HashSet;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirectiveType;

public class CompilerSwitchRegistry {
  private final Multimap<CompilerDirectiveType, Range<Integer>> rangesByDirectiveType;

  CompilerSwitchRegistry() {
    rangesByDirectiveType =
        Multimaps.newSetMultimap(Maps.newEnumMap(CompilerDirectiveType.class), HashSet::new);
  }

  void addSwitch(CompilerDirectiveType type, int startIndex, int endIndex) {
    rangesByDirectiveType.put(type, Range.closed(startIndex, endIndex));
  }

  public boolean isActiveSwitch(CompilerDirectiveType type, int tokenIndex) {
    return rangesByDirectiveType.get(type).stream().anyMatch(range -> range.contains(tokenIndex));
  }
}
