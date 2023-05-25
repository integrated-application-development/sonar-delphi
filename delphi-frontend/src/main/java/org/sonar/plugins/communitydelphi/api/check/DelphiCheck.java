package org.sonar.plugins.communitydelphi.api.check;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;

public abstract class DelphiCheck implements DelphiParserVisitor<DelphiCheckContext> {
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
