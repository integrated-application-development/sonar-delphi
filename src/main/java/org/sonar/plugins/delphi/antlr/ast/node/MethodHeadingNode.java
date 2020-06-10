package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.Collections;
import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.DelphiParser;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameter;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.type.Type;

public final class MethodHeadingNode extends DelphiNode {
  private String image;
  private Boolean isClassMethod;
  private String typeName;
  private String qualifiedName;
  private MethodKind methodKind;

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
      image = fullyQualifiedName() + getParameterSignature();
    }
    return image;
  }

  private String getParameterSignature() {
    MethodParametersNode parameters = getMethodParametersNode();
    return parameters != null ? getMethodParametersNode().getImage() : "";
  }

  public MethodNameNode getMethodNameNode() {
    return (MethodNameNode) jjtGetChild(1);
  }

  public MethodParametersNode getMethodParametersNode() {
    return getFirstChildOfType(MethodParametersNode.class);
  }

  public List<FormalParameterData> getParameters() {
    MethodParametersNode parameters = getMethodParametersNode();
    return parameters != null ? parameters.getParameters() : Collections.emptyList();
  }

  public List<Type> getParameterTypes() {
    MethodParametersNode parameters = getMethodParametersNode();
    return parameters != null ? parameters.getParameterTypes() : Collections.emptyList();
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

  public String simpleName() {
    return getMethodNameNode().simpleName();
  }

  public String fullyQualifiedName() {
    if (qualifiedName == null) {
      MethodHeadingNode node = this;
      StringBuilder name = new StringBuilder();

      while (node != null) {
        String methodName = getMethodNameNode().simpleNameWithTypeParameters();

        if (name.length() != 0) {
          name.insert(0, ".");
        }

        name.insert(0, methodName);
        node = findParentMethodHeading(node);
      }

      if (!getTypeName().isEmpty()) {
        name.insert(0, getTypeName() + ".");
      }

      name.insert(0, findUnitName() + ".");

      qualifiedName = name.toString();
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

  private String getTypeNameForMethodImplementation() {
    MethodHeadingNode heading = findParentMethodHeading(this);
    if (heading == null) {
      String methodName = getMethodNameNode().fullyQualifiedName();
      int dotIndex = methodName.lastIndexOf('.');
      return (dotIndex > 0) ? methodName.substring(0, dotIndex) : "";
    }
    return heading.getTypeName();
  }

  private String getTypeNameForMethodDeclaration() {
    TypeDeclarationNode type = getFirstParentOfType(TypeDeclarationNode.class);
    if (type != null) {
      return type.qualifiedNameExcludingUnit();
    }
    return "";
  }

  public boolean isMethodImplementation() {
    return jjtGetParent() instanceof MethodImplementationNode;
  }

  public boolean isMethodDeclaration() {
    return jjtGetParent() instanceof MethodDeclarationNode;
  }

  public MethodKind getMethodKind() {
    if (methodKind == null) {
      methodKind = MethodKind.fromTokenType(jjtGetChildId(0));
    }
    return methodKind;
  }

  private static MethodHeadingNode findParentMethodHeading(MethodHeadingNode headingNode) {
    MethodImplementationNode parentMethod =
        headingNode.jjtGetParent().getFirstParentOfType(MethodImplementationNode.class);
    return parentMethod == null ? null : parentMethod.getMethodHeading();
  }
}
