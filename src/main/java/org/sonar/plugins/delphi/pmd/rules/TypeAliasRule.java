package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;

public class TypeAliasRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(TypeDeclarationNode typeDeclaration, RuleContext data) {
    if (typeDeclaration.isTypeAlias() || typeDeclaration.isTypeType()) {
      addViolation(data, typeDeclaration);
    }
    return super.visit(typeDeclaration, data);
  }
}
