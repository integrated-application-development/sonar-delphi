package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ExceptItemNode extends DelphiNode {

  public ExceptItemNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Nullable
  public IdentifierNode getExceptionIdentifier() {
    return hasExceptionIdentifier() ? (IdentifierNode) jjtGetChild(0) : null;
  }

  @NotNull
  public QualifiedIdentifierNode getExceptionTypeIdentifier() {
    return (QualifiedIdentifierNode) jjtGetChild(hasExceptionIdentifier() ? 1 : 0);
  }

  @Nullable
  public StatementNode getStatement() {
    Node statement = jjtGetChild(hasExceptionIdentifier() ? 3 : 2);
    return statement instanceof StatementNode ? (StatementNode) statement : null;
  }

  private boolean hasExceptionIdentifier() {
    return jjtGetChild(0).jjtGetId() == DelphiLexer.TkIdentifier;
  }
}
