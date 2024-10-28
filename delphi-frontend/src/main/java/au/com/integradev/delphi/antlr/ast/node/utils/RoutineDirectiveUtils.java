/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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
package au.com.integradev.delphi.antlr.ast.node.utils;

import com.google.common.collect.ImmutableSet;
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public final class RoutineDirectiveUtils {
  private RoutineDirectiveUtils() {
    // Utility class
  }

  public static Set<RoutineDirective> getDirectives(DelphiNode node) {
    var builder = new ImmutableSet.Builder<RoutineDirective>();
    for (DelphiNode child : node.getChildren()) {
      DelphiToken token = child.getToken();
      RoutineDirective directive = RoutineDirective.fromToken(token);
      if (directive != null) {
        builder.add(directive);
      }
    }
    return builder.build();
  }
}
