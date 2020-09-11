package org.sonar.plugins.delphi.symbol.declaration;

import static com.google.common.collect.Iterables.getLast;
import static java.util.Collections.emptySet;

import com.google.common.collect.ComparisonChain;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.delphi.antlr.ast.node.GenericDefinitionNode.TypeParameter;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNameNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.SimpleNameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.Visibility;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.declaration.parameter.FormalParameter;
import org.sonar.plugins.delphi.symbol.declaration.parameter.IntrinsicParameter;
import org.sonar.plugins.delphi.symbol.declaration.parameter.Parameter;
import org.sonar.plugins.delphi.symbol.resolve.Invocable;
import org.sonar.plugins.delphi.type.DelphiProceduralType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicMethodData;

public final class MethodNameDeclaration extends AbstractDelphiNameDeclaration
    implements GenerifiableDeclaration, TypedDeclaration, Invocable, Visibility {
  private final String fullyQualifiedName;
  private final List<Parameter> parameters;
  private final Type returnType;
  private final Set<MethodDirective> directives;
  private final boolean isClassInvocable;
  private final boolean isCallable;
  private final boolean isVariadic;
  private final MethodKind methodKind;
  private final Type methodType;
  private final TypeNameDeclaration typeDeclaration;
  private final VisibilityType visibility;
  private final List<TypedDeclaration> typeParameters;

  private final Set<UnitNameDeclaration> dependencies;
  private int hashCode;

  private MethodNameDeclaration(
      SymbolicNode location,
      String fullyQualifiedName,
      List<Parameter> parameters,
      Type returnType,
      Set<MethodDirective> directives,
      boolean isClassInvocable,
      boolean isCallable,
      boolean isVariadic,
      MethodKind methodKind,
      Type methodType,
      @Nullable TypeNameDeclaration typeDeclaration,
      VisibilityType visibility,
      List<TypedDeclaration> typeParameters) {
    super(location);
    this.fullyQualifiedName = fullyQualifiedName;
    this.parameters = parameters;
    this.returnType = returnType;
    this.directives = directives;
    this.isClassInvocable = isClassInvocable;
    this.isCallable = isCallable;
    this.methodKind = methodKind;
    this.methodType = methodType;
    this.typeDeclaration = typeDeclaration;
    this.visibility = visibility;
    this.isVariadic = isVariadic;
    this.typeParameters = typeParameters;
    this.dependencies = new HashSet<>();
  }

  public static MethodNameDeclaration create(SymbolicNode node, IntrinsicMethodData data) {
    return new MethodNameDeclaration(
        node,
        "System." + data.getMethodName(),
        data.getParameters().stream()
            .map(IntrinsicParameter::create)
            .collect(Collectors.toUnmodifiableList()),
        data.getReturnType(),
        emptySet(),
        false,
        true,
        data.isVariadic(),
        data.getMethodKind(),
        data.createMethodType(),
        null,
        VisibilityType.PUBLIC,
        Collections.emptyList());
  }

  public static MethodNameDeclaration create(MethodNode method) {
    MethodNameNode nameNode = method.getMethodNameNode();
    SimpleNameDeclarationNode declarationNode = nameNode.getNameDeclarationNode();
    DelphiNode location = (declarationNode == null) ? nameNode : declarationNode.getIdentifier();

    boolean isCallable =
        !((method.isDestructor()
                || method.isConstructor()
                || method.getMethodKind() == MethodKind.OPERATOR)
            && method.isClassMethod());

    return new MethodNameDeclaration(
        new SymbolicNode(location),
        method.fullyQualifiedName(),
        extractParameterDeclarations(method),
        method.getReturnType(),
        method.getDirectives(),
        method.isClassMethod(),
        isCallable,
        false,
        method.getMethodKind(),
        DelphiProceduralType.method(extractParameterTypes(method), method.getReturnType()),
        method.getTypeDeclaration(),
        method.getVisibility(),
        extractGenericTypeParameters(method));
  }

  private static List<Parameter> extractParameterDeclarations(MethodNode method) {
    return method.getParameters().stream()
        .map(FormalParameter::create)
        .collect(Collectors.toUnmodifiableList());
  }

  private static List<Type> extractParameterTypes(MethodNode method) {
    return method.getParameters().stream()
        .map(FormalParameterData::getType)
        .collect(Collectors.toUnmodifiableList());
  }

  private static List<TypedDeclaration> extractGenericTypeParameters(MethodNode method) {
    MethodNameNode methodName = method.getMethodHeading().getMethodNameNode();
    NameDeclarationNode declaration = methodName.getNameDeclarationNode();
    if (declaration != null) {
      return declaration.getTypeParameters().stream()
          .map(TypeParameter::getLocation)
          .map(NameDeclarationNode::getNameDeclaration)
          .map(TypedDeclaration.class::cast)
          .collect(Collectors.toUnmodifiableList());
    }
    return Collections.emptyList();
  }

  @Override
  public List<Parameter> getParameters() {
    return parameters;
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
    return methodType;
  }

  public String fullyQualifiedName() {
    return fullyQualifiedName;
  }

  public Set<MethodDirective> getDirectives() {
    return directives;
  }

  public boolean hasDirective(MethodDirective directive) {
    return getDirectives().contains(directive);
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
  public int getParametersCount() {
    return isVariadic ? 255 : Invocable.super.getParametersCount();
  }

  @Override
  public Parameter getParameter(int index) {
    if (index < parameters.size()) {
      return parameters.get(index);
    } else if (isVariadic) {
      return getLast(parameters);
    }

    throw new IndexOutOfBoundsException(
        "Invalid parameter declaration access (Size:"
            + parameters.size()
            + " Index:"
            + index
            + ")");
  }

  @Override
  public List<TypedDeclaration> getTypeParameters() {
    return typeParameters;
  }

  public void addDependency(UnitNameDeclaration dependency) {
    dependencies.add(dependency);
  }

  public Set<UnitNameDeclaration> getDependencies() {
    return dependencies;
  }

  @Override
  public MethodNameDeclaration doSpecialization(TypeSpecializationContext context) {
    return new MethodNameDeclaration(
        getNode(),
        fullyQualifiedName,
        parameters.stream()
            .map(parameter -> parameter.specialize(context))
            .collect(Collectors.toUnmodifiableList()),
        returnType.specialize(context),
        directives,
        isClassInvocable,
        isCallable,
        isVariadic,
        methodKind,
        methodType.specialize(context),
        typeDeclaration,
        visibility,
        typeParameters.stream()
            .map(parameter -> parameter.specialize(context))
            .map(TypedDeclaration.class::cast)
            .collect(Collectors.toUnmodifiableList()));
  }

  @Override
  public boolean equals(Object other) {
    if (super.equals(other)) {
      MethodNameDeclaration that = (MethodNameDeclaration) other;
      return fullyQualifiedName.equalsIgnoreCase(that.fullyQualifiedName)
          && parameters.equals(that.parameters)
          && returnType.is(that.returnType)
          && directives.equals(that.directives)
          && isCallable == that.isCallable
          && isClassInvocable == that.isClassInvocable
          && typeParameters.equals(that.typeParameters);
    }
    return false;
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode =
          Objects.hash(
              super.hashCode(),
              fullyQualifiedName.toLowerCase(),
              parameters,
              returnType.getImage().toLowerCase(),
              directives,
              isCallable,
              isClassInvocable,
              typeParameters);
    }
    return hashCode;
  }

  @Override
  public int compareTo(@NotNull DelphiNameDeclaration other) {
    int result = super.compareTo(other);
    if (result == 0) {
      MethodNameDeclaration that = (MethodNameDeclaration) other;
      result =
          ComparisonChain.start()
              .compare(getParametersCount(), that.getParametersCount())
              .compare(getRequiredParametersCount(), that.getRequiredParametersCount())
              .compare(directives.size(), that.directives.size())
              .compareTrueFirst(isCallable, that.isCallable)
              .compareTrueFirst(isClassInvocable, that.isClassInvocable)
              .compare(returnType.getImage(), that.returnType.getImage())
              .compare(fullyQualifiedName, that.fullyQualifiedName, String.CASE_INSENSITIVE_ORDER)
              .compare(typeParameters.size(), that.typeParameters.size())
              .result();

      if (result != 0) {
        return result;
      }

      if (!equals(other)) {
        result = -1;
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return "Method "
        + node.getImage()
        + ", line "
        + node.getBeginLine()
        + ", params = "
        + parameters.size()
        + " <"
        + getNode().getUnitName()
        + ">";
  }
}
