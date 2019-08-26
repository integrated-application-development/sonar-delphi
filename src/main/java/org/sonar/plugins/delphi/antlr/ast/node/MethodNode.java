package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameter;

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

  public String getSimpleName() {
    return getMethodHeading().getSimpleName();
  }

  public String getQualifiedName() {
    return getMethodHeading().getQualifiedName();
  }

  public List<FormalParameter> getParameters() {
    return getMethodHeading().getParameters();
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
}
