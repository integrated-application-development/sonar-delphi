package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.VarNameDeclarationListNode;

public class MultipleVariableDeclarationRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(VarNameDeclarationListNode declarationList, RuleContext data) {
    if (declarationList.getIdentifiers().size() > 1) {
      addViolation(data, declarationList);
    }
    return super.visit(declarationList, data);
  }
}
