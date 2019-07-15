package org.sonar.plugins.delphi.pmd.rules;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * This rule will find any instances of a constructor which are not declared with 'Create' at the
 * beginning.
 *
 * <p>To find this, at each 'constructor' declaration, there will next be a 'TkFunctionArgs' node,
 * followed by a node containing the name of the declared constructor, this is the node to check
 * for the correct naming convention</p>
 */
public class ConstructorCreateRule extends NameConventionRule {
  private static final String PREFIX = "Create";

  @Override
  public DelphiPMDNode findNameNode(DelphiPMDNode node) {
    if (isInterfaceSection() && node.getType() == DelphiLexer.CONSTRUCTOR) {
      Tree functionName = node.getFirstChildWithType(DelphiLexer.TkFunctionName);

      if (functionName != null) {
        return new DelphiPMDNode((CommonTree) functionName.getChild(0), node.getASTTree());
      }
    }

    return null;
  }

  @Override
  protected boolean isViolation(DelphiPMDNode node) {
    String name = node.getText();
    return !name.equals(PREFIX) && !compliesWithPrefixNamingConvention(name, PREFIX);
  }
}
