package org.sonar.plugins.communitydelphi.api.check;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

public abstract class DelphiCheck implements DelphiParserVisitor<DelphiCheckContext> {
  /**
   * A rule parameter that allows the user to override the rule scope specified in the metadata
   * JSON.
   *
   * <p>This parameter is only surfaced for template rules.
   */
  @RuleProperty(
      key = "scope",
      description =
          "The type of code this rule should apply to. Options are: 'ALL', 'MAIN', 'TEST'.")
  public String customRuleScopeOverride = "";

  public void start(DelphiCheckContext context) {
    // do nothing
  }

  public void end(DelphiCheckContext context) {
    // do nothing
  }

  protected void reportIssue(DelphiCheckContext context, DelphiNode node, String message) {
    context.newIssue().onNode(node).withMessage(message).report();
  }
}
