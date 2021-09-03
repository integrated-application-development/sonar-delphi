package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.Scope;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnaryExpressionNode;
import org.sonar.plugins.delphi.operator.UnaryOperator;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.MethodScope;

public class AddressOfNestedMethodRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(UnaryExpressionNode expression, RuleContext data) {
    if (isAddressOfSubProcedure(expression)) {
      addViolation(data, expression);
    }

    return super.visit(expression, data);
  }

  private static boolean isAddressOfSubProcedure(UnaryExpressionNode expression) {
    if (expression.getOperator() != UnaryOperator.ADDRESS) {
      return false;
    }

    if (!(expression.getOperand() instanceof PrimaryExpressionNode)) {
      return false;
    }

    PrimaryExpressionNode primary = (PrimaryExpressionNode) expression.getOperand();
    if (primary.jjtGetNumChildren() != 1) {
      return false;
    }

    Node name = primary.jjtGetChild(0);
    if (!(name instanceof NameReferenceNode)) {
      return false;
    }

    NameDeclaration declaration = ((NameReferenceNode) name).getLastName().getNameDeclaration();
    if (!(declaration instanceof MethodNameDeclaration)) {
      return false;
    }

    Scope scope = declaration.getScope();
    return scope instanceof MethodScope && scope.getParent() instanceof MethodScope;
  }
}
