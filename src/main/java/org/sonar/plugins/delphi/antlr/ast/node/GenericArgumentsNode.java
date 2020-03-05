package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class GenericArgumentsNode extends DelphiNode {
  private String image;

  public GenericArgumentsNode(Token token) {
    super(token);
  }

  public GenericArgumentsNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<TypeNode> getTypeArguments() {
    return findChildrenOfType(TypeNode.class);
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = buildImage();
    }
    return image;
  }

  private String buildImage() {
    StringBuilder builder = new StringBuilder();
    builder.append("<");

    for (TypeNode typeNode : getTypeArguments()) {
      if (builder.length() > 1) {
        builder.append(",");
      }

      if (typeNode instanceof TypeReferenceNode) {
        TypeReferenceNode typeReference = (TypeReferenceNode) typeNode;
        if (typeReference.getTypeDeclaration() != null) {
          builder.append(typeReference.getImage());
        } else {
          builder.append(typeReference.getNameNode().getImage());
        }
      } else {
        builder.append(typeNode.getImage());
      }
    }

    builder.append(">");

    return builder.toString();
  }
}
