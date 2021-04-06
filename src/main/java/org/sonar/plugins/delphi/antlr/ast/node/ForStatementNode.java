package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;

public abstract class ForStatementNode extends StatementNode {
  protected ForStatementNode(Token token) {
    super(token);
  }

  public ForLoopVarNode getVariable() {
    return (ForLoopVarNode) jjtGetChild(0);
  }

  public abstract StatementNode getStatement();
}
