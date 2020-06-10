package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.Collections;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.type.DelphiProceduralType;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;

public final class AnonymousMethodNode extends ExpressionNode {
  private String image;
  private MethodKind methodKind;

  public AnonymousMethodNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public MethodParametersNode getMethodParametersNode() {
    return getFirstChildOfType(MethodParametersNode.class);
  }

  public MethodReturnTypeNode getReturnTypeNode() {
    return getFirstChildOfType(MethodReturnTypeNode.class);
  }

  public Type getReturnType() {
    return getReturnTypeNode().getTypeNode().getType();
  }

  private MethodKind getMethodKind() {
    if (methodKind == null) {
      methodKind = MethodKind.fromTokenType(jjtGetId());
    }
    return methodKind;
  }

  public boolean isFunction() {
    return getMethodKind() == MethodKind.FUNCTION;
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

  @Override
  @NotNull
  public Type createType() {
    MethodParametersNode parameters = getMethodParametersNode();
    MethodReturnTypeNode returnTypeNode = getReturnTypeNode();

    return DelphiProceduralType.anonymous(
        parameters == null ? Collections.emptyList() : parameters.getParameterTypes(),
        returnTypeNode == null ? DelphiType.voidType() : returnTypeNode.getTypeNode().getType());
  }
}
