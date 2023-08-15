/*
 * Copyright (C) 2023 My Organization
 * This code is released under [MIT No Attribution](https://opensource.org/licenses/MIT-0) license.
 */
package au.com.integradev.samples.delphi.checks;

import java.util.Locale;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.MethodDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.RuleTemplate;

/**
 * This class is an example of how to implement your own template rules. The rule raises a minor
 * issue when a method name contains a forbidden string.
 */
@RuleTemplate
@Rule(key = "StringInMethodName")
public class StringInMethodNameCheck extends DelphiCheck {
  private static final String DEFAULT_MESSAGE = "Rename this method containing a forbidden string.";

  @RuleProperty(description = "String to forbid in method names")
  public String string = "";

  @RuleProperty(key = "message", description = "The issue message", defaultValue = DEFAULT_MESSAGE)
  public String message = DEFAULT_MESSAGE;

  /**
   * Overriding the visitor method to implement the logic of the rule.
   *
   * @param node AST node of the visited method
   */
  @Override
  public DelphiCheckContext visit(MethodDeclarationNode node, DelphiCheckContext context) {
    // Code located before the call to the overridden method is executed before visiting the node

    if (node.simpleName().toUpperCase(Locale.ROOT).contains(string.toUpperCase(Locale.ROOT))) {
      // Adds an issue by attaching it with the node and the rule
      reportIssue(context, node, message);
    }

    // The call to the super implementation allows to continue the visit of the AST.
    // Be careful to always call this method to visit every node of the AST.
    return super.visit(node, context);

    // Code located after the call to the overridden method is executed when leaving the node
  }
}
