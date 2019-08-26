package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class AnonymousMethodNode extends ExpressionNode {
  private String image;

  public AnonymousMethodNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  private MethodParametersNode getMethodParametersNode() {
    return getFirstChildOfType(MethodParametersNode.class);
  }

  private MethodReturnTypeNode getReturnTypeNode() {
    return getFirstChildOfType(MethodReturnTypeNode.class);
  }

  private String getParameterSignature() {
    MethodParametersNode parameters = getMethodParametersNode();
    return parameters != null ? getMethodParametersNode().getImage() : "";
  }

  private String getReturnTypeSignature() {
    MethodReturnTypeNode returnType = getReturnTypeNode();
    return returnType != null ? (" : " + getReturnTypeNode().getTypeNode().getImage()) : "";
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = getToken().getImage() + getParameterSignature() + getReturnTypeSignature();
    }
    return image;
  }
}
