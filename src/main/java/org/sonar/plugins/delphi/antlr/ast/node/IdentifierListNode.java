package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class IdentifierListNode extends DelphiNode {
  private List<IdentifierNode> identifiers;

  public IdentifierListNode(Token token) {
    super(token);
  }

  public IdentifierListNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<IdentifierNode> getIdentifiers() {
    if (identifiers == null) {
      identifiers = findChildrenOfType(IdentifierNode.class);
    }
    return identifiers;
  }
}
