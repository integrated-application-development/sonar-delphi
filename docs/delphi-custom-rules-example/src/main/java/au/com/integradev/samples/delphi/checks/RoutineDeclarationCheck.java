/*
 * Copyright (C) 2023 My Organization
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package au.com.integradev.samples.delphi.checks;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.RoutineDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;

/**
 * This class is an example of how to implement your own rules. The (stupid) rule raises a minor
 * issue each time a routine is encountered.
 */
@Rule(key = "RoutineDeclaration")
public class RoutineDeclarationCheck extends DelphiCheck {
  /**
   * Overriding the visitor method to implement the logic of the rule.
   *
   * @param node AST node of the visited routine
   */
  @Override
  public DelphiCheckContext visit(RoutineDeclarationNode node, DelphiCheckContext context) {
    // Code located before the call to the overridden method is executed before visiting the node

    // Adds an issue by attaching it with the node and the rule
    reportIssue(context, node, "Avoid declaring routines (don't ask why)");

    // The call to the super implementation allows to continue the visit of the AST.
    // Be careful to always call this method to visit every node of the AST.
    return super.visit(node, context);

    // Code located after the call to the overridden method is executed when leaving the node
  }
}
