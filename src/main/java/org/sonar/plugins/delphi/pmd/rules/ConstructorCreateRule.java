package org.sonar.plugins.delphi.pmd.rules;

import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

/**
 * This rule will find any instances of a constructor which are not declared with 'Create' at the
 * beginning.
 *
 * <p>To find this, at each 'constructor' declaration, there will next be a 'TkFunctionArgs' node,
 * followed by a node containing the name of the declared constructor, this is the node to check for
 * the correct naming convention
 */
public class ConstructorCreateRule extends NameConventionRule {
  private static final String PREFIX = "Create";

  @Override
  public DelphiNode findNode(DelphiNode node) {
    if (isInterfaceSection() && node.getType() == DelphiLexer.CONSTRUCTOR) {
      Tree functionName = node.getFirstChildWithType(DelphiLexer.TkFunctionName);

      if (functionName != null) {
        return (DelphiNode) functionName.getChild(0);
      }
    }

    return null;
  }

  @Override
  protected boolean isViolation(DelphiNode node) {
    String name = node.getText();
    return !name.equals(PREFIX) && !compliesWithPrefixNamingConvention(name, PREFIX);
  }
}
