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
package au.com.integradev.delphi.utils;

import static au.com.integradev.delphi.utils.StatementUtils.isRoutineInvocation;

import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RaiseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineBodyNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;

public final class RoutineUtils {
  private RoutineUtils() {
    // Utility class
  }

  public static boolean isStubRoutineWithStackUnwinding(RoutineImplementationNode routine) {
    RoutineBodyNode body = routine.getRoutineBody();
    if (body.hasStatementBlock()) {
      for (StatementNode statement : body.getStatementBlock().getStatements()) {
        if (isStackUnwindingStatement(statement)) {
          return true;
        } else if (!(statement instanceof AssignmentStatementNode)) {
          return false;
        }
      }
    }
    return false;
  }

  private static boolean isStackUnwindingStatement(StatementNode statement) {
    return statement instanceof RaiseStatementNode || isAssertFalse(statement);
  }

  private static boolean isAssertFalse(StatementNode statement) {
    return isRoutineInvocation(
        statement, "System.Assert", arguments -> ExpressionNodeUtils.isFalse(arguments.get(0)));
  }
}
