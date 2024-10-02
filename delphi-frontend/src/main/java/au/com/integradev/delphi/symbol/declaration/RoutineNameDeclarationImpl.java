/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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

import au.com.integradev.delphi.symbol.SymbolicNode;
import au.com.integradev.delphi.type.factory.TypeFactoryImpl;
import au.com.integradev.delphi.type.intrinsic.IntrinsicRoutine;
import au.com.integradev.delphi.type.parameter.FormalParameter;
import au.com.integradev.delphi.type.parameter.IntrinsicParameter;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ComparisonChain;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.AttributeListNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.GenericDefinitionNode.TypeParameter;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineHeadingNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineNameNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineNode;
import org.sonar.plugins.communitydelphi.api.ast.SimpleNameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType.ProceduralKind;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

public final class RoutineNameDeclarationImpl extends NameDeclarationImpl
    implements RoutineNameDeclaration {
  private final String fullyQualifiedName;
  private final Type returnType;
  private final Set<RoutineDirective> directives;
  private final boolean isClassInvocable;
  private final boolean isCallable;
  private final RoutineKind routineKind;
  private final ProceduralType routineType;
  private final TypeNameDeclaration typeDeclaration;
  private final VisibilityType visibility;
  private final List<TypedDeclaration> typeParameters;
  private final List<Type> attributeTypes;

  private final Set<UnitNameDeclaration> dependencies;
  private int hashCode;

  @VisibleForTesting
  public RoutineNameDeclarationImpl(
      SymbolicNode location,
      String fullyQualifiedName,
      Type returnType,
      Set<RoutineDirective> directives,
      boolean isClassInvocable,
      boolean isCallable,
      RoutineKind routineKind,
      ProceduralType routineType,
      @Nullable TypeNameDeclaration typeDeclaration,
      VisibilityType visibility,
      List<TypedDeclaration> typeParameters,
      List<Type> attributeTypes) {
    super(location);
    this.fullyQualifiedName = fullyQualifiedName;
    this.returnType = returnType;
    this.directives = directives;
    this.isClassInvocable = isClassInvocable;
    this.isCallable = isCallable;
    this.routineKind = routineKind;
    this.routineType = routineType;
    this.typeDeclaration = typeDeclaration;
    this.visibility = visibility;
    this.typeParameters = typeParameters;
    this.attributeTypes = attributeTypes;
    this.dependencies = new HashSet<>();
  }

  public static RoutineNameDeclaration create(
      SymbolicNode node, IntrinsicRoutine data, TypeFactory typeFactory) {
    return new RoutineNameDeclarationImpl(
        node,
        data.fullyQualifiedName(),
        data.getReturnType(),
        emptySet(),
        false,
        true,
        data.getRoutineKind(),
        ((TypeFactoryImpl) typeFactory)
            .createProcedural(
                ProceduralKind.ROUTINE,
                createParameters(data),
                data.getReturnType(),
                data.isVariadic() ? Set.of(RoutineDirective.VARARGS) : Collections.emptySet()),
        null,
        VisibilityType.PUBLIC,
        Collections.emptyList(),
        Collections.emptyList());
  }

  public static RoutineNameDeclarationImpl create(RoutineNode routine, TypeFactory typeFactory) {
    RoutineNameNode nameNode = routine.getRoutineNameNode();
    SimpleNameDeclarationNode declarationNode = nameNode.getNameDeclarationNode();
    DelphiNode location = (declarationNode == null) ? nameNode : declarationNode.getIdentifier();

    boolean isCallable =
        !((routine.isDestructor()
                || routine.isConstructor()
                || routine.getRoutineKind() == RoutineKind.OPERATOR)
            && routine.isClassMethod());

    List<Type> attributeTypes;
    RoutineHeadingNode routineHeading = routine.getRoutineHeading();
    AttributeListNode attributeList = routineHeading.getAttributeList();
    if (attributeList != null) {
      attributeTypes = attributeList.getAttributeTypes();
    } else {
      attributeTypes = Collections.emptyList();
    }

    return new RoutineNameDeclarationImpl(
        new SymbolicNode(location),
        routine.fullyQualifiedName(),
        routine.getReturnType(),
        routine.getDirectives(),
        routine.isClassMethod(),
        isCallable,
        routine.getRoutineKind(),
        ((TypeFactoryImpl) typeFactory)
            .createProcedural(
                ProceduralKind.ROUTINE,
                createParameters(routine),
                routine.getReturnType(),
                routine.getDirectives()),
        routine.getTypeDeclaration(),
        routine.getVisibility(),
        extractGenericTypeParameters(routine),
        attributeTypes);
  }

  private static List<Parameter> createParameters(RoutineNode routine) {
    return routine.getParameters().stream()
        .map(FormalParameter::create)
        .collect(Collectors.toUnmodifiableList());
  }

  private static List<Parameter> createParameters(IntrinsicRoutine data) {
    return data.getParameters().stream()
        .map(IntrinsicParameter::create)
        .collect(Collectors.toUnmodifiableList());
  }

  private static List<TypedDeclaration> extractGenericTypeParameters(RoutineNode routine) {
    RoutineNameNode routineName = routine.getRoutineHeading().getRoutineNameNode();
    NameDeclarationNode declaration = routineName.getNameDeclarationNode();
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
    return routineType.parameters();
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

  @Override
  public RoutineKind getRoutineKind() {
    return routineKind;
  }

  @Override
  public Type getType() {
    return routineType;
  }

  @Override
  public String fullyQualifiedName() {
    return fullyQualifiedName;
  }

  @Override
  public Set<RoutineDirective> getDirectives() {
    return directives;
  }

  @Override
  public boolean hasDirective(RoutineDirective directive) {
    return getDirectives().contains(directive);
  }

  @Override
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
    return routineType.parametersCount();
  }

  @Override
  public Parameter getParameter(int index) {
    return routineType.getParameter(index);
  }

  @Override
  public List<TypedDeclaration> getTypeParameters() {
    return typeParameters;
  }

  @Override
  public List<Type> getAttributeTypes() {
    return attributeTypes;
  }

  public void addDependency(UnitNameDeclaration dependency) {
    dependencies.add(dependency);
  }

  public Set<UnitNameDeclaration> getDependencies() {
    return dependencies;
  }

  @Override
  public RoutineNameDeclaration doSpecialization(TypeSpecializationContext context) {
    return new RoutineNameDeclarationImpl(
        node,
        fullyQualifiedName,
        returnType.specialize(context),
        directives,
        isClassInvocable,
        isCallable,
        routineKind,
        (ProceduralType) routineType.specialize(context),
        typeDeclaration,
        visibility,
        typeParameters.stream()
            .map(parameter -> parameter.specialize(context))
            .map(TypedDeclaration.class::cast)
            .collect(Collectors.toUnmodifiableList()),
        attributeTypes);
  }

  @Override
  public boolean equals(Object other) {
    if (super.equals(other)) {
      RoutineNameDeclarationImpl that = (RoutineNameDeclarationImpl) other;
      return fullyQualifiedName.equalsIgnoreCase(that.fullyQualifiedName)
          && getParameters().equals(that.getParameters())
          && returnType.is(that.returnType)
          && directives.equals(that.directives)
          && isCallable == that.isCallable
          && isClassInvocable == that.isClassInvocable
          && typeParameters.equals(that.typeParameters)
          && attributeTypes.equals(that.attributeTypes);
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
              typeParameters,
              attributeTypes);
    }
    return hashCode;
  }

  @Override
  public int compareTo(NameDeclaration other) {
    int result = super.compareTo(other);
    if (result == 0) {
      RoutineNameDeclarationImpl that = (RoutineNameDeclarationImpl) other;
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
    return "Routine "
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
