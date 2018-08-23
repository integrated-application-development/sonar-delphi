package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class EmptyBracketsRule extends DelphiRule {

  /**
   * This rule searches for any calls to methods which include brackets () but do not pass any
   * parameters. This is not necessary in Delphi so warnings are raised.
   *
   * @param node the current node
   * @param ctx the ruleContext to store the violations
   */
  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {

    // Look for any method call nodes under a begin block ('TkIdentifier'), check if the next two are parentheses
    if (node.getType() == DelphiLexer.BEGIN) {

      for (int i = 0; i < node.getChildCount() - 1; i++) {

        DelphiPMDNode childNode = (DelphiPMDNode) node.getChild(i);
        if (childNode.getType() == DelphiLexer.TkIdentifier) {
          if (node.getChild(i + 1).getType() == DelphiLexer.LPAREN) {
            if (node.getChild(i + 2).getType() == DelphiLexer.RPAREN) {
              // The next node and the subsequent nodes were parentheses, no arguments passed
              addViolation(ctx, childNode);
            }
          }
        }

      }
    }

  }

}
