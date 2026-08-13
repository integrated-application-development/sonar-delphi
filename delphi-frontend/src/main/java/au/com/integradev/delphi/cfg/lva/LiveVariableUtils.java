/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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
package au.com.integradev.delphi.cfg.lva;

import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class LiveVariableUtils {
  private LiveVariableUtils() {
    // Util class
  }

  public static Collector<LiveVariable, ?, Set<LiveVariable>> toLiveVariableSet() {
    return Collectors.toCollection(
        () -> new TreeSet<>(Comparator.comparing(LiveVariable::getNameDeclaration)));
  }

  public static Set<LiveVariable> newLiveVariableSet() {
    return new TreeSet<>(Comparator.comparing(LiveVariable::getNameDeclaration));
  }
}
