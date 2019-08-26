package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.RaiseStatementNode;

public class RaisingGeneralExceptionRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(RaiseStatementNode raise, RuleContext data) {
    if (isRaisingGeneralException(raise)) {
      addViolation(data, raise);
    }
    return super.visit(raise, data);
  }

  private static boolean isRaisingGeneralException(RaiseStatementNode raise) {
    ExpressionNode expr = raise.getRaiseExpression();
    if (expr == null) {
      return false;
    }

    ExpressionNode primary = expr.skipParentheses();
    if (!(primary instanceof PrimaryExpressionNode)) {
      return false;
    }

    return (primary.jjtGetNumChildren() >= 3
        && primary.jjtGetChild(0).hasImageEqualTo("Exception")
        && primary.jjtGetChild(1).hasImageEqualTo(".")
        && primary.jjtGetChild(2).hasImageEqualTo("Create"));
  }
}
