package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

public class ConstantNotationRule extends DelphiRule {

  /**
   * This rule will find a 'const' block, and search through it's child nodes for assignments made
   * to constant values. When one is found, the node before it is considered the name of the
   * declared constant. This is then checked for correct notation and if not beginning with C_, a
   * violation is raised in SonarQube.
   *
   * @param node the current node
   * @param ctx the ruleContext to store the violations
   */
  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (node.getType() != DelphiLexer.CONST) {
      return;
    }
    // For every child in a const block (except the last), check if any are equals nodes (type 50)
    for (int i = 0; i < node.getChildCount() - 1; i++) {
      Tree childNode = node.getChild(i);
      if (childNode != null && childNode.getType() == DelphiLexer.EQUAL) {
        // Get the node before the equals node, as that will be the name used to define it
        CommonTree assignmentNode = (CommonTree)node.getChild(i - 1);
        String constName = assignmentNode.getText();

        if (!constName.startsWith("C_")) {
          addViolation(ctx, new DelphiPMDNode(assignmentNode, node.getASTTree()));
        }
      }
    }
  }
}
