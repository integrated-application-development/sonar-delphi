package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;

public abstract class ForStatementNode extends StatementNode {
  protected ForStatementNode(Token token) {
    super(token);
  }

  public NameReferenceNode getVariable() {
    return (NameReferenceNode) jjtGetChild(0);
  }
}
