package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class VarNameDeclarationListNode extends DelphiNode {
  private List<VarNameDeclarationNode> identifiers;

  public VarNameDeclarationListNode(Token token) {
    super(token);
  }

  public VarNameDeclarationListNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<VarNameDeclarationNode> getIdentifiers() {
    if (identifiers == null) {
      identifiers = findChildrenOfType(VarNameDeclarationNode.class);
    }
    return identifiers;
  }
}
