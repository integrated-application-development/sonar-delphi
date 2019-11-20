package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ExceptBlockNode extends DelphiNode {
  private List<ExceptItemNode> handlers;

  public ExceptBlockNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Nullable
  public StatementListNode getStatementList() {
    if (isBareExcept()) {
      return (StatementListNode) jjtGetChild(0);
    }
    return null;
  }

  public List<ExceptItemNode> getHandlers() {
    if (handlers == null) {
      handlers = findChildrenOfType(ExceptItemNode.class);
    }
    return handlers;
  }

  @Nullable
  public ElseBlockNode getElseBlock() {
    return getFirstChildOfType(ElseBlockNode.class);
  }

  public boolean isBareExcept() {
    return jjtGetChild(0) instanceof StatementListNode;
  }

  public boolean hasHandlers() {
    return !getHandlers().isEmpty();
  }
}
