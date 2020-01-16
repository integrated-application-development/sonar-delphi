package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.Collections;
import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameter;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class MethodHeadingNode extends DelphiNode {
  private String image;
  private Boolean isClassMethod;
  private String typeName;
  private String qualifiedName;

  public MethodHeadingNode(Token token) {
    super(token);
  }

  public MethodHeadingNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = (getQualifiedName() + getParameterSignature()).toLowerCase();
    }
    return image;
  }

  private String getParameterSignature() {
    MethodParametersNode parameters = getMethodParametersNode();
    return parameters != null ? getMethodParametersNode().getImage() : "";
  }

  public MethodNameNode getMethodName() {
    return (MethodNameNode) jjtGetChild(1);
  }

  public MethodParametersNode getMethodParametersNode() {
    return getFirstChildOfType(MethodParametersNode.class);
  }

  public List<FormalParameter> getParameters() {
    MethodParametersNode parameters = getMethodParametersNode();
    return parameters != null ? parameters.getParameters() : Collections.emptyList();
  }

  public MethodReturnTypeNode getMethodReturnType() {
    return getFirstChildOfType(MethodReturnTypeNode.class);
  }

  public boolean isClassMethod() {
    if (isClassMethod == null) {
      isClassMethod = getFirstChildWithId(DelphiLexer.CLASS) != null;
    }
    return isClassMethod;
  }

  public String getSimpleName() {
    return getMethodName().getFirstChildOfType(QualifiedIdentifierNode.class).getSimpleName();
  }

  public String getQualifiedName() {
    if (qualifiedName == null) {
      if (isMethodImplementation()) {
        qualifiedName = getQualifiedNameForMethodImplementation();
      } else if (isMethodDeclaration()) {
        qualifiedName = getQualifiedNameForMethodDeclaration();
      }
    }

    return qualifiedName;
  }

  public String getTypeName() {
    if (typeName == null) {
      if (isMethodImplementation()) {
        typeName = getTypeNameForMethodImplementation();
      } else if (isMethodDeclaration()) {
        typeName = getTypeNameForMethodDeclaration();
      }
    }
    return typeName;
  }

  private String getQualifiedNameForMethodImplementation() {
    MethodHeadingNode node = this;
    StringBuilder name = new StringBuilder();

    while (node != null) {
      String methodName =
          node.getMethodName()
              .getFirstChildOfType(QualifiedIdentifierNode.class)
              .getQualifiedName();

      if (name.length() != 0) {
        name.insert(0, ".");
      }

      name.insert(0, methodName);
      node = findParentMethodHeading(node);
    }

    return name.toString();
  }

  private String getQualifiedNameForMethodDeclaration() {
    return getTypeName() + "." + getSimpleName();
  }

  private String getTypeNameForMethodImplementation() {
    MethodHeadingNode heading = findParentMethodHeading(this);
    if (heading == null) {
      String methodName = getQualifiedName();
      int dotIndex = methodName.lastIndexOf('.');
      return (dotIndex > 0) ? methodName.substring(0, dotIndex) : "";
    }
    return heading.getTypeName();
  }

  private String getTypeNameForMethodDeclaration() {
    TypeDeclarationNode type = getFirstParentOfType(TypeDeclarationNode.class);
    if (type != null) {
      return type.getQualifiedName();
    }
    return "";
  }

  public boolean isMethodImplementation() {
    return jjtGetParent() instanceof MethodImplementationNode;
  }

  public boolean isMethodDeclaration() {
    return jjtGetParent() instanceof MethodDeclarationNode;
  }

  public boolean isConstructor() {
    return jjtGetChildId(0) == DelphiLexer.CONSTRUCTOR;
  }

  public boolean isDestructor() {
    return jjtGetChildId(0) == DelphiLexer.DESTRUCTOR;
  }

  public boolean isFunction() {
    return jjtGetChildId(0) == DelphiLexer.FUNCTION;
  }

  public boolean isOperator() {
    return jjtGetChildId(0) == DelphiLexer.OPERATOR;
  }

  public boolean isProcedure() {
    return jjtGetChildId(0) == DelphiLexer.PROCEDURE;
  }

  private static MethodHeadingNode findParentMethodHeading(MethodHeadingNode headingNode) {
    MethodImplementationNode parentMethod =
        headingNode.jjtGetParent().getFirstParentOfType(MethodImplementationNode.class);
    return parentMethod == null ? null : parentMethod.getMethodHeading();
  }
}
