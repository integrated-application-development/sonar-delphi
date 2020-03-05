package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationListNode;

public class MultipleVariableDeclarationRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(NameDeclarationListNode declarationList, RuleContext data) {
    if (declarationList.getDeclarations().size() > 1) {
      addViolation(data, declarationList);
    }
    return super.visit(declarationList, data);
  }
}
