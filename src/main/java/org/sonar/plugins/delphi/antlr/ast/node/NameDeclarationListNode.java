package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class NameDeclarationListNode extends DelphiNode {
  private List<NameDeclarationNode> declarations;

  public NameDeclarationListNode(Token token) {
    super(token);
  }

  public NameDeclarationListNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<NameDeclarationNode> getDeclarations() {
    if (declarations == null) {
      declarations = findChildrenOfType(NameDeclarationNode.class);
    }
    return declarations;
  }
}
