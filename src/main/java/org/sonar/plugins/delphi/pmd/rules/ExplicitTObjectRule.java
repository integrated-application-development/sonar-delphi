package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;

public class ExplicitTObjectRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(TypeDeclarationNode type, RuleContext data) {
    if (type.isClass()
        && type.getTypeNode().getParentTypeNodes().isEmpty()
        && !type.isForwardDeclaration()) {
      addViolation(data, type.getTypeNameNode());
    }
    return super.visit(type, data);
  }
}
