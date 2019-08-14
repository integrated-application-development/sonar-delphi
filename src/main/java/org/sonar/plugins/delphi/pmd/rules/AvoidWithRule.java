package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public class AvoidWithRule extends DelphiRule {

  @Override
  public void visit(DelphiNode node, RuleContext ctx) {
    if (node.getType() == DelphiLexer.WITH) {
      addViolation(ctx, node);
    }
  }
}
