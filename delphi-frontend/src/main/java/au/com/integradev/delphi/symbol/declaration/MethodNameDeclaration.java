/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.symbol.declaration;

import static java.util.Collections.emptySet;

import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.GenericDefinitionNode.TypeParameter;
import org.sonar.plugins.communitydelphi.api.ast.MethodNameNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.SimpleNameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.Visibility;
import au.com.integradev.delphi.symbol.NameDeclaration;
import au.com.integradev.delphi.symbol.SymbolicNode;
import au.com.integradev.delphi.symbol.resolve.Invocable;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Type.ProceduralType;
import au.com.integradev.delphi.type.factory.TypeFactory;
import au.com.integradev.delphi.type.generic.TypeSpecializationContext;
import au.com.integradev.delphi.type.intrinsic.IntrinsicMethod;
import au.com.integradev.delphi.type.parameter.FormalParameter;
import au.com.integradev.delphi.type.parameter.IntrinsicParameter;
import au.com.integradev.delphi.type.parameter.Parameter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ComparisonChain;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class MethodNameDeclaration extends AbstractNameDeclaration
    implements GenerifiableDeclaration, TypedDeclaration, Invocable, Visibility {
  private final String fullyQualifiedName;
  private final Type returnType;
  private final Set<MethodDirective> directives;
  private final boolean isClassInvocable;
  private final boolean isCallable;
  private final MethodKind methodKind;
  private final ProceduralType methodType;
  private final TypeNameDeclaration typeDeclaration;
  private final VisibilityType visibility;
  private final List<TypedDeclaration> typeParameters;

  private final Set<UnitNameDeclaration> dependencies;
  private int hashCode;

  @VisibleForTesting
  public MethodNameDeclaration(
      SymbolicNode location,
      String fullyQualifiedName,
      Type returnType,
      Set<MethodDirective> directives,
      boolean isClassInvocable,
      boolean isCallable,
      MethodKind methodKind,
      ProceduralType methodType,
      @Nullable TypeNameDeclaration typeDeclaration,
      VisibilityType visibility,
      List<TypedDeclaration> typeParameters) {
    super(location);
    this.fullyQualifiedName = fullyQualifiedName;
    this.returnType = returnType;
    this.directives = directives;
    this.isClassInvocable = isClassInvocable;
    this.isCallable = isCallable;
    this.methodKind = methodKind;
    this.methodType = methodType;
    this.typeDeclaration = typeDeclaration;
    this.visibility = visibility;
    this.typeParameters = typeParameters;
    this.dependencies = new HashSet<>();
  }

  public static MethodNameDeclaration create(
      SymbolicNode node, IntrinsicMethod data, TypeFactory typeFactory) {
    return new MethodNameDeclaration(
        node,
        data.fullyQualifiedName(),
        data.getReturnType(),
        emptySet(),
        false,
        true,
        data.getMethodKind(),
        typeFactory.method(createParameters(data), data.getReturnType(), data.isVariadic()),
        null,
        VisibilityType.PUBLIC,
        Collections.emptyList());
  }

  public static MethodNameDeclaration create(MethodNode method, TypeFactory typeFactory) {
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
        method.getReturnType(),
        method.getDirectives(),
        method.isClassMethod(),
        isCallable,
        method.getMethodKind(),
        typeFactory.method(createParameters(method), method.getReturnType()),
        method.getTypeDeclaration(),
        method.getVisibility(),
        extractGenericTypeParameters(method));
  }

  private static List<Parameter> createParameters(MethodNode method) {
    return method.getParameters().stream()
        .map(FormalParameter::create)
        .collect(Collectors.toUnmodifiableList());
  }

  private static List<Parameter> createParameters(IntrinsicMethod data) {
    return data.getParameters().stream()
        .map(IntrinsicParameter::create)
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
    return methodType.parameters();
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
    return methodType.parametersCount();
  }

  @Override
  public Parameter getParameter(int index) {
    return methodType.getParameter(index);
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
        node,
        fullyQualifiedName,
        returnType.specialize(context),
        directives,
        isClassInvocable,
        isCallable,
        methodKind,
        (ProceduralType) methodType.specialize(context),
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
          && getParameters().equals(that.getParameters())
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
              getParameters(),
              returnType.getImage().toLowerCase(),
              directives,
              isCallable,
              isClassInvocable,
              typeParameters);
    }
    return hashCode;
  }

  @Override
  public int compareTo(@NotNull NameDeclaration other) {
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
        + getNode().getImage()
        + ", line "
        + getNode().getBeginLine()
        + ", params = "
        + getParameters().size()
        + " <"
        + getNode().getUnitName()
        + ">";
  }
}
