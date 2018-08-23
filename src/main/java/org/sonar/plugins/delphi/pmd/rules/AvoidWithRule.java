package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class AvoidWithRule extends DelphiRule {

  private String sonarMessage;

  protected void init() {
    super.init();
    sonarMessage = "Use of 'with' should be avoided";
  }

  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (node.getType() == DelphiLexer.WITH) {
      addViolation(ctx, node);
    }
  }

}
