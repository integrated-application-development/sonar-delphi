package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class IdentifierConventionRule extends DelphiRule {

  /**
   * This rule looks at all identifiers at ensures it follows the Delphi Case convention, at least
   * the first character should be uppercase
   *
   * @param node the current node
   * @param ctx the ruleContext to store the violations
   */
  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    // Within a var block, look for variable identifiers
    if (node.getType() == DelphiLexer.VAR) {
      for (int i = 0; i < node.getChildCount() - 1; i++) {
        Tree childNode = node.getChild(i);
        if (childNode.getType() == DelphiLexer.TkVariableIdents) {
          // Name identifier will be in the next node
          String identifier = childNode.getChild(0).getText();
          char firstChar = identifier.charAt(0);
          if (!Character.isUpperCase(firstChar)) {
            addViolation(ctx, (DelphiPMDNode) childNode);
          }

        }
      }
    }
  }
}

