package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public class ReRaiseExceptionRule extends DelphiRule {
  /**
   * This rule looks for exception blocks where the exception is raised a second time at the end of
   * the except block. This is done by searching for any begin block (except blocks always contain
   * begin/end blocks) and any children which are 'raise' statements. If the next node after the
   * raise statement is not a semicolon, it is assumed this is a re-raised exception and a violation
   * is raised.
   *
   * @param node the current node
   * @param ctx the ruleContext to store the violations
   */
  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (node.getType() != DelphiLexer.BEGIN) {
      // Exception blocks always contain 'begin' statements, so look at the children of begin blocks
      // Note: Would be good to just access the next node after a 'raise' but list isn't accessible
      // here
      return;
    }

    String exceptionName = null;

    for (int i = 0; i < node.getChildCount() - 1; i++) {
      DelphiPMDNode childNode = (DelphiPMDNode) node.getChild(i);

      if (childNode.getType() == DelphiLexer.ON) {
        // The next node after an 'on' statement will be the name of the exception
        exceptionName = node.getChild(i + 1).getText();
      }

      if (childNode.getType() == DelphiLexer.RAISE) {
        DelphiPMDNode exceptionDeclarationNode = (DelphiPMDNode) node.getChild(i + 1);

        // Only raise exception if the declared name is re raised later on
        if (exceptionDeclarationNode != null
            && exceptionDeclarationNode.getText().equals(exceptionName)) {
          addViolation(ctx, childNode);
        }
      }
    }
  }
}
