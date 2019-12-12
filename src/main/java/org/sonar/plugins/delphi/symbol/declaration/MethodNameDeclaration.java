package org.sonar.plugins.delphi.symbol.declaration;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.DelphiToken;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameter;
import org.sonar.plugins.delphi.antlr.ast.node.MethodHeadingNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodHeadingNode.MethodKind;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.Visibility;
import org.sonar.plugins.delphi.symbol.resolve.Invocable;
import org.sonar.plugins.delphi.type.DelphiProceduralType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Typed;

public final class MethodNameDeclaration extends DelphiNameDeclaration
    implements Typed, Invocable, Visibility {
  private final String qualifiedName;
  private final List<ParameterDeclaration> parameterDeclarations;
  private final Type returnType;
  private final Set<MethodDirective> directives;
  private final boolean isClassInvocable;
  private final boolean isCallable;
  private final MethodKind methodKind;
  private final ProceduralType type;
  private final TypeNameDeclaration typeDeclaration;
  private final VisibilityType visibility;

  private int hashCode;

  public MethodNameDeclaration(MethodNode method) {
    super(method.getMethodNameNode());
    this.qualifiedName = method.fullyQualifiedName();
    this.parameterDeclarations = extractParameterDeclarations(method);
    this.returnType = method.getReturnType();
    this.directives = extractDirectives(method);
    this.isClassInvocable = method.isClassMethod();
    this.isCallable = !((method.isDestructor() || method.isConstructor()) && isClassInvocable);
    this.methodKind = method.getMethodKind();
    this.type = DelphiProceduralType.method(extractParameterTypes(method), returnType);
    this.typeDeclaration = method.getTypeDeclaration();
    this.visibility = method.getVisibility();
  }

  private static List<ParameterDeclaration> extractParameterDeclarations(MethodNode method) {
    return method.getParameters().stream()
        .map(ParameterDeclaration::new)
        .collect(Collectors.toUnmodifiableList());
  }

  private static List<Type> extractParameterTypes(MethodNode method) {
    return method.getParameters().stream()
        .map(FormalParameter::getType)
        .collect(Collectors.toUnmodifiableList());
  }

  private static Set<MethodDirective> extractDirectives(MethodNode method) {
    var builder = new ImmutableSet.Builder<MethodDirective>();
    MethodHeadingNode heading = method.getMethodHeading();
    for (int i = 0; i < heading.jjtGetNumChildren(); ++i) {
      DelphiToken token = ((DelphiNode) heading.jjtGetChild(i)).getToken();
      MethodDirective directive = MethodDirective.fromToken(token);
      if (directive != null) {
        builder.add(directive);
      }
    }
    return builder.build();
  }

  @Override
  public List<ParameterDeclaration> getParameters() {
    return parameterDeclarations;
  }

  @Override
  public Type getReturnType() {
    return returnType;
  }

  @Override
  public boolean isCallable() {
    return isCallable;
  }

  @Override
  public boolean isClassInvocable() {
    return isClassInvocable;
  }

  public MethodKind getMethodKind() {
    return methodKind;
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }

  public String fullyQualifiedName() {
    return qualifiedName;
  }

  public Set<MethodDirective> getDirectives() {
    return directives;
  }

  @Nullable
  public TypeNameDeclaration getTypeDeclaration() {
    return typeDeclaration;
  }

  @Override
  public VisibilityType getVisibility() {
    return visibility;
  }

  @Override
  public String toString() {
    return "Method "
        + node.getImage()
        + ", line "
        + node.getBeginLine()
        + ", params = "
        + parameterDeclarations.size()
        + " <"
        + getNode().getUnitName()
        + ">";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MethodNameDeclaration that = (MethodNameDeclaration) o;
    return getImage().equalsIgnoreCase(that.getImage())
        && qualifiedName.equalsIgnoreCase(that.qualifiedName)
        && parameterDeclarations.equals(that.parameterDeclarations)
        && returnType.equals(that.returnType)
        && directives.equals(that.directives)
        && isCallable == that.isCallable
        && isClassInvocable == that.isClassInvocable;
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode =
          Objects.hash(
              getImage().toLowerCase(),
              qualifiedName.toLowerCase(),
              parameterDeclarations,
              returnType,
              directives,
              isCallable,
              isClassInvocable);
    }
    return hashCode;
  }
}
