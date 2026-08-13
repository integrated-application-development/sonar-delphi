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

import au.com.integradev.delphi.cfg.api.Block;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;

class LocalDataFlowProperties {

  private final Set<LiveVariable> usedBeforeAssigned =
      new TreeSet<>(Comparator.comparing(LiveVariable::getNameDeclaration));
  private final Set<LiveVariable> assigned =
      new TreeSet<>(Comparator.comparing(LiveVariable::getNameDeclaration));

  /// Returns the identifiers used before being assigned in this block. For a block N, GEN\[N\]:
  /// Variables that are using in N and are not preceded by a definition within N.
  ///
  /// @return Identifiers used before being assigned in this block
  public Set<LiveVariable> getUsedBeforeAssigned() {
    return usedBeforeAssigned;
  }

  /// Returns the identifiers assigned in this block. For a block N, KILL\[N\]: Variables for which
  /// there is a definition within N.
  ///
  /// @return Identifiers assigned in this block
  public Set<LiveVariable> getAssigned() {
    return assigned;
  }

  LocalDataFlowProperties(
      Block block, Function<LiveVariable, Collection<LiveVariable>> getLiveVariables) {
    new BlockDataFlowVisitor()
        .withOnAssign(assigned::add)
        .withOnReference(
            liveVariable -> {
              for (LiveVariable variable : getLiveVariables.apply(liveVariable)) {
                if (assigned.stream()
                    .map(LiveVariable::getNameDeclaration)
                    .noneMatch(variable.getNameDeclaration()::equals)) {
                  usedBeforeAssigned.add(variable);
                }
              }
            })
        .visit(block);
  }
}
