package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class WithStatementNode extends StatementNode {
  public WithStatementNode(Token token) {
    super(token);
  }

  public List<ExpressionNode> getTargets() {
    return findChildrenOfType(ExpressionNode.class);
  }

  @Nullable
  public StatementNode getStatement() {
    return getFirstChildOfType(StatementNode.class);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }
}
