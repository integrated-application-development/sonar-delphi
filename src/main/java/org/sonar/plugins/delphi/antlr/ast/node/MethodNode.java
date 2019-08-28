package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameter;
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

  public boolean isConstructor() {
    return getMethodHeading().isConstructor();
  }

  public boolean isDestructor() {
    return getMethodHeading().isDestructor();
  }

  public boolean isFunction() {
    return getMethodHeading().isFunction();
  }

  public boolean isOperator() {
    return getMethodHeading().isOperator();
  }

  public boolean isProcedure() {
    return getMethodHeading().isProcedure();
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
