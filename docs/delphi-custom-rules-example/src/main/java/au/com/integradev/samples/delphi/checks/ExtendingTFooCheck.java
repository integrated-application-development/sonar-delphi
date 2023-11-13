/*
 * Copyright (C) 2023 My Organization
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package au.com.integradev.samples.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.type.Type;

/**
 * This class is an example of how to implement your own rules. The rule raises a minor issue when a
 * type inherits from the <code>Foos.TFoo</code> type.
 */
@Rule(key = "ExtendingTFoo")
public class ExtendingTFooCheck extends DelphiCheck {
  private static final String TFOO = "Foos.TFoo";

  /**
   * Overriding the visitor method to implement the logic of the rule.
   *
   * @param node AST node of the visited type declaration
   */
  @Override
  public DelphiCheckContext visit(TypeDeclarationNode node, DelphiCheckContext context) {
    // Code located before the call to the overridden method is executed before visiting the node

    Type type = node.getType();
    if (type.isDescendantOf(TFOO)) {
      reportIssue(context, node.getTypeNameNode(), "Extending 'Foos.TFoo' is forbidden.");
    }

    return super.visit(node, context);

    // Code located after the call to the overridden method is executed when leaving the node
  }
}
