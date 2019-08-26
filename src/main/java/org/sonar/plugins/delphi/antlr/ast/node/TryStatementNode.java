package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class TryStatementNode extends StatementNode {
  private ExceptBlockNode exceptBlock;
  private FinallyBlockNode finallyBlock;

  public TryStatementNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public StatementListNode getStatementList() {
    return (StatementListNode) jjtGetChild(0);
  }

  public boolean hasExceptBlock() {
    return jjtGetChild(1) instanceof ExceptBlockNode;
  }

  public boolean hasFinallyBlock() {
    return jjtGetChild(1) instanceof FinallyBlockNode;
  }

  public ExceptBlockNode getExpectBlock() {
    if (exceptBlock == null && hasExceptBlock()) {
      exceptBlock = (ExceptBlockNode) jjtGetChild(1);
    }
    return exceptBlock;
  }

  public FinallyBlockNode getFinallyBlock() {
    if (finallyBlock == null && hasFinallyBlock()) {
      finallyBlock = (FinallyBlockNode) jjtGetChild(1);
    }
    return finallyBlock;
  }
}
