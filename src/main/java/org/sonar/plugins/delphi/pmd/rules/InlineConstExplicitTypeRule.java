package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.ConstStatementNode;

public class InlineConstExplicitTypeRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(ConstStatementNode node, RuleContext data) {
    if (node.getTypeNode() == null) {
      addViolation(data, node);
    }
    return super.visit(node, data);
  }
}
