package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class TypeParameterNode extends DelphiNode {
  public TypeParameterNode(Token token) {
    super(token);
  }

  public TypeParameterNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<NameDeclarationNode> getTypeParameterNameNodes() {
    return findChildrenOfType(NameDeclarationNode.class);
  }

  public List<TypeReferenceNode> getTypeConstraintNodes() {
    return findChildrenOfType(TypeReferenceNode.class);
  }
}
