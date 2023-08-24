/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.preprocessor;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Range;
import java.util.HashSet;
import org.sonar.plugins.communitydelphi.api.directive.SwitchDirective.SwitchKind;

public class CompilerSwitchRegistry {
  private final Multimap<SwitchKind, Range<Integer>> rangesBySwitchKind;

  CompilerSwitchRegistry() {
    rangesBySwitchKind = Multimaps.newSetMultimap(Maps.newEnumMap(SwitchKind.class), HashSet::new);
  }

  void addSwitch(SwitchKind kind, int startIndex, int endIndex) {
    rangesBySwitchKind.put(kind, Range.closed(startIndex, endIndex));
  }

  public boolean isActiveSwitch(SwitchKind kind, int tokenIndex) {
    return rangesBySwitchKind.get(kind).stream().anyMatch(range -> range.contains(tokenIndex));
  }
}
