package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.delphi.symbol.declaration.MethodDirective;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;

public abstract class MethodNode extends DelphiNode implements Visibility {
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

  public MethodNameNode getMethodNameNode() {
    return getMethodHeading().getMethodNameNode();
  }

  public String simpleName() {
    return getMethodHeading().simpleName();
  }

  public String fullyQualifiedName() {
    return getMethodHeading().fullyQualifiedName();
  }

  public List<FormalParameterData> getParameters() {
    return getMethodHeading().getParameters();
  }

  public List<Type> getParameterTypes() {
    return getMethodHeading().getParameterTypes();
  }

  public Type getReturnType() {
    if (isProcedure() || isConstructor()) {
      return DelphiType.voidType();
    }

    MethodReturnTypeNode returnTypeNode = getMethodHeading().getMethodReturnType();
    if (returnTypeNode != null) {
      return returnTypeNode.getTypeNode().getType();
    }

    return DelphiType.unknownType();
  }

  public MethodKind getMethodKind() {
    return getMethodHeading().getMethodKind();
  }

  public Set<MethodDirective> getDirectives() {
    return getMethodHeading().getDirectives();
  }

  public boolean hasDirective(MethodDirective directive) {
    return getDirectives().contains(directive);
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

  @Override
  public final VisibilityType getVisibility() {
    MethodNameDeclaration declaration = getMethodNameDeclaration();
    if (declaration != null) {
      return declaration.getVisibility();
    } else {
      return createVisibility();
    }
  }

  @Nullable
  public MethodNameDeclaration getMethodNameDeclaration() {
    return getMethodHeading().getMethodNameNode().getMethodNameDeclaration();
  }

  @Nullable
  public abstract TypeNameDeclaration getTypeDeclaration();

  protected abstract VisibilityType createVisibility();
}
