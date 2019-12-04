package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class CaseStatementNode extends StatementNode {
  public CaseStatementNode(Token token) {
    super(token);
  }

  public List<CaseItemStatementNode> getCaseItems() {
    return findChildrenOfType(CaseItemStatementNode.class);
  }

  @Nullable
  public ElseBlockNode getElseBlockNode() {
    return getFirstChildOfType(ElseBlockNode.class);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }
}
