package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;


public class ConstructorCreateRule extends DelphiRule {

  private int CONSTRUCTOR_NODE_TYPE_POS;
  private int CONSTRUCTOR_NAME_POS;

  @Override
  protected void init() {
    super.init();
    // The first child node in constructor nodes is the TkFunctionName node
    CONSTRUCTOR_NODE_TYPE_POS = 0;
    // The first child position is the name of the constructor in TkFunctionName
    CONSTRUCTOR_NAME_POS = 0;
  }

  /**
   * This rule will find any instances of a constructor which are not declared with 'Create' at the
   * beginning.
   *
   * To find this, at each 'constructor' declaration, there will next be a 'TkFunctionArgs' node,
   * followed by a node containing the name of the declared constructor, this is the node to check
   * for the correct naming convention
   *
   * @param node the current node
   * @param ctx the ruleContext to store the violations
   */
  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {

    if (node.getType() == DelphiLexer.CONSTRUCTOR) {

      Tree constructorTypeNode = node.getChild(CONSTRUCTOR_NODE_TYPE_POS);

      if (constructorTypeNode.getType() == DelphiLexer.TkFunctionName) {

        Tree constructorNameNode = constructorTypeNode.getChild(CONSTRUCTOR_NAME_POS);
        String constructorName = constructorNameNode.getText();

        if (!constructorName.startsWith("Create")) {
          addViolation(ctx, node);
        }

      }

    }

  }
}
