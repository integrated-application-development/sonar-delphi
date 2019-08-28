package org.sonar.plugins.delphi.pmd.rules;

import java.util.regex.Pattern;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.RaiseStatementNode;

public class RaisingGeneralExceptionRule extends AbstractDelphiRule {

  private static final Pattern EXCEPTION_CREATE = Pattern.compile("(?i)Exception.Create\\b.*");

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

    return EXCEPTION_CREATE.matcher(primary.getImage()).matches();
  }
}
