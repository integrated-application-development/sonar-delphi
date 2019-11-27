package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameter;
import org.sonar.plugins.delphi.antlr.ast.node.MethodHeadingNode.MethodKind;
import org.sonar.plugins.delphi.symbol.TypeNameDeclaration;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;

public abstract class MethodNode extends DelphiNode {
  public MethodNode(Token token) {
    super(token);
  }

  public MethodNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public String getImage() {
    return getMethodHeading().getImage();
  }

  public MethodHeadingNode getMethodHeading() {
    return (MethodHeadingNode) jjtGetChild(0);
  }

  public String simpleName() {
    return getMethodHeading().simpleName();
  }

  public String fullyQualifiedName() {
    return getMethodHeading().fullyQualifiedName();
  }

  public List<FormalParameter> getParameters() {
    return getMethodHeading().getParameters();
  }

  public List<Type> getParameterTypes() {
    return getMethodHeading().getParameterTypes();
  }

  public Type getReturnType() {
    if (isProcedure()) {
      return DelphiType.voidType();
    }

    if (isConstructor()) {
      TypeNameDeclaration declaration = getTypeDeclaration();
      if (declaration != null) {
        return declaration.getType();
      }
    }

    MethodReturnTypeNode returnTypeNode = getMethodHeading().getMethodReturnType();
    if (returnTypeNode != null) {
      return returnTypeNode.getTypeNode().getType();
    }

    return DelphiType.unknownType();
  }

  public FormalParameter getParameter(String image) {
    for (FormalParameter parameter : getParameters()) {
      if (parameter.getImage().equals(image)) {
        return parameter;
      }
    }
    return null;
  }

  public MethodKind getMethodKind() {
    return getMethodHeading().getMethodKind();
  }

  public boolean isConstructor() {
    return getMethodKind() == MethodKind.CONSTRUCTOR;
  }

  public boolean isDestructor() {
    return getMethodKind() == MethodKind.DESTRUCTOR;
  }

  public boolean isFunction() {
    return getMethodKind() == MethodKind.FUNCTION;
  }

  public boolean isOperator() {
    return getMethodKind() == MethodKind.OPERATOR;
  }

  public boolean isProcedure() {
    return getMethodKind() == MethodKind.PROCEDURE;
  }

  public boolean isClassMethod() {
    return getMethodHeading().isClassMethod();
  }

  public String getTypeName() {
    return getMethodHeading().getTypeName();
  }

  public abstract DelphiNode getMethodName();

  @Nullable
  public abstract TypeNameDeclaration getTypeDeclaration();
}
