package org.sonar.plugins.delphi.antlr.ast.node;

import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class IfStatementNode extends StatementNode {
  public IfStatementNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public ExpressionNode getGuardExpression() {
    return (ExpressionNode) jjtGetChild(0);
  }

  private CommonDelphiNode getElseToken() {
    return (CommonDelphiNode) getFirstChildWithId(DelphiLexer.ELSE);
  }

  public boolean hasElseBranch() {
    return getElseToken() != null;
  }

  @Nullable
  public StatementNode getThenStatement() {
    Node node = jjtGetChild(2);
    if (node instanceof StatementNode) {
      return (StatementNode) node;
    }
    return null;
  }

  @Nullable
  public StatementNode getElseStatement() {
    if (hasElseBranch()) {
      return (StatementNode) jjtGetChild(getElseToken().jjtGetChildIndex() + 1);
    }
    return null;
  }
}
