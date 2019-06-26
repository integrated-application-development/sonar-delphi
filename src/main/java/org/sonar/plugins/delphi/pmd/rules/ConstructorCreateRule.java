package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;


public class ConstructorCreateRule extends DelphiRule {

  private int constructorNodeTypePos;
  private int constructorNodeNamePos;

  @Override
  protected void init() {
    super.init();
    // The first child node in constructor nodes is the TkFunctionName node
    constructorNodeTypePos = 0;
    // The first child position is the name of the constructor in TkFunctionName
    constructorNodeNamePos = 0;
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

      Tree constructorTypeNode = node.getChild(constructorNodeTypePos);

      if (constructorTypeNode.getType() == DelphiLexer.TkFunctionName) {

        Tree constructorNameNode = constructorTypeNode.getChild(constructorNodeNamePos);
        String constructorName = constructorNameNode.getText();

        if (!constructorName.startsWith("Create")) {
          addViolation(ctx, node);
        }
      }
    }
  }


  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
