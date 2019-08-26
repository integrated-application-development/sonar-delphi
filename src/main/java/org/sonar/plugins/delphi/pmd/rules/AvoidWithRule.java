package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.pmd.FilePosition;

public class AvoidWithRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(DelphiNode node, RuleContext data) {
    if (node.jjtGetId() == DelphiLexer.WITH) {
      newViolation(data).atPosition(FilePosition.from(node.getToken())).atLocation(node).save();
    }
    return super.visit(node, data);
  }
}
