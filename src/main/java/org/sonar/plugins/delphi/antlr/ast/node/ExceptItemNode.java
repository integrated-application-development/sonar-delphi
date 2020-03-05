package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public final class ExceptItemNode extends DelphiNode implements Typed {

  public ExceptItemNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @NotNull
  @Override
  public Type getType() {
    return getExceptionType().getType();
  }

  @Nullable
  public NameDeclarationNode getExceptionName() {
    return hasExceptionName() ? (NameDeclarationNode) jjtGetChild(0) : null;
  }

  @NotNull
  public TypeReferenceNode getExceptionType() {
    return (TypeReferenceNode) jjtGetChild(hasExceptionName() ? 1 : 0);
  }

  @Nullable
  public StatementNode getStatement() {
    Node statement = jjtGetChild(hasExceptionName() ? 3 : 2);
    return statement instanceof StatementNode ? (StatementNode) statement : null;
  }

  private boolean hasExceptionName() {
    return jjtGetChild(0) instanceof NameDeclarationNode;
  }
}
