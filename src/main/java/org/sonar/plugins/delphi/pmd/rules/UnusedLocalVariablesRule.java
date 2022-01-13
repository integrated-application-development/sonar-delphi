package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarStatementNode;
import org.sonar.plugins.delphi.symbol.scope.MethodScope;

public class UnusedLocalVariablesRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(VarDeclarationNode varDeclaration, RuleContext data) {
    if (varDeclaration.getScope() instanceof MethodScope) {
      varDeclaration.getNameDeclarationList().getDeclarations().stream()
          .filter(node -> node.getUsages().isEmpty())
          .forEach(node -> addViolation(data, node));
    }
    return data;
  }

  @Override
  public RuleContext visit(VarStatementNode varStatement, RuleContext data) {
    varStatement.getNameDeclarationList().getDeclarations().stream()
        .filter(node -> node.getUsages().isEmpty())
        .forEach(node -> addViolation(data, node));
    return data;
  }
}
