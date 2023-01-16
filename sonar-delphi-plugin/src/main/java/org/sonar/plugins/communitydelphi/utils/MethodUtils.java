/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package org.sonar.plugins.communitydelphi.utils;

import static org.sonar.plugins.communitydelphi.utils.StatementUtils.isMethodInvocation;

import org.sonar.plugins.communitydelphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.RaiseStatementNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.StatementNode;

public class MethodUtils {
  private MethodUtils() {
    // Utility class
  }

  public static boolean isMethodStubWithStackUnwinding(MethodImplementationNode method) {
    MethodBodyNode body = method.getMethodBody();
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
    return isMethodInvocation(statement, "System.Assert", arguments -> arguments.get(0).isFalse());
  }
}
