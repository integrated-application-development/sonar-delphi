package org.sonar.plugins.delphi.utils;

import static org.sonar.plugins.delphi.utils.StatementUtils.isMethodInvocation;

import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.RaiseStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;

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
