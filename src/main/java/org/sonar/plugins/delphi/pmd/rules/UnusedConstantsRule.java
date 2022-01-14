package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.ConstDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.ConstStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;

public class UnusedConstantsRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(ConstDeclarationNode constDeclaration, RuleContext data) {
    NameDeclarationNode name = constDeclaration.getNameDeclarationNode();
    if (name.getUsages().isEmpty()) {
      addViolation(data, name);
    }
    return data;
  }

  @Override
  public RuleContext visit(ConstStatementNode constStatement, RuleContext data) {
    NameDeclarationNode name = constStatement.getNameDeclarationNode();
    if (name.getUsages().isEmpty()) {
      addViolation(data, name);
    }
    return data;
  }
}
