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
package au.com.integradev.delphi.type.factory;

import static java.util.function.Predicate.not;

import au.com.integradev.delphi.symbol.scope.TypeScopeImpl;
import org.sonar.plugins.communitydelphi.api.type.StructKind;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;
import au.com.integradev.delphi.type.generic.GenerifiableTypeImpl;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;

public class StructTypeImpl extends GenerifiableTypeImpl implements StructType {
  private final List<ImagePart> imageParts;
  private final int size;
  private DelphiScope scope;
  private Set<Type> parents;
  private StructKind kind;
  private Type superType;
  private Set<Type> typesWithImplicitConversionsToThis;
  private Set<Type> typesWithImplicitConversionsFromThis;
  private boolean isForwardType;

  StructTypeImpl(
      List<ImagePart> imageParts, int size, DelphiScope scope, Set<Type> parents, StructKind kind) {
    this.imageParts = imageParts;
    this.size = size;
    this.scope = scope;
    this.kind = kind;
    setParents(parents);
  }

  private void setParents(Set<Type> parents) {
    this.parents = Set.copyOf(parents);
    if (isInterface()) {
      this.superType = Iterables.getFirst(parents, TypeFactory.unknownType());
    } else {
      this.superType =
          this.parents.stream()
              .filter(StructTypeImpl.class::isInstance)
              .filter(not(Type::isInterface))
              .findFirst()
              .orElse(TypeFactory.unknownType());
    }
  }

  @Override
  public String getImage() {
    return imageParts.stream().map(ImagePart::toString).collect(Collectors.joining("."));
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isSubTypeOf(String image) {
    for (Type parent : parents) {
      if (parent.is(image) || parent.isSubTypeOf(image)) {
        return true;
      }
    }
    return false;
  }

  @NotNull
  @Override
  public Type superType() {
    return superType;
  }

  @Override
  public Set<Type> parents() {
    return parents;
  }

  @Override
  public boolean isClass() {
    return kind == StructKind.CLASS;
  }

  @Override
  public boolean isInterface() {
    return kind == StructKind.INTERFACE;
  }

  @Override
  public boolean isRecord() {
    return kind == StructKind.RECORD;
  }

  @Override
  public final boolean isStruct() {
    return true;
  }

  @Override
  @NotNull
  public DelphiScope typeScope() {
    return scope;
  }

  @Override
  public StructKind kind() {
    return kind;
  }

  @Override
  public boolean isForwardType() {
    return isForwardType;
  }

  /**
   * Adds the full type declaration's information. Also marks this StructType instance as a forward
   * type.
   *
   * @param fullType Type representing the full type declaration
   */
  public void setFullType(StructType fullType) {
    this.scope = fullType.typeScope();
    this.parents = ImmutableSet.copyOf(fullType.parents());
    this.kind = fullType.kind();
    this.superType = fullType.superType();
    this.isForwardType = true;
  }

  @Override
  public Set<Type> typesWithImplicitConversionsFromThis() {
    if (typesWithImplicitConversionsFromThis == null) {
      indexImplicitConversions();
    }

    return typesWithImplicitConversionsFromThis;
  }

  @Override
  public Set<Type> typesWithImplicitConversionsToThis() {
    if (typesWithImplicitConversionsToThis == null) {
      indexImplicitConversions();
    }
    return typesWithImplicitConversionsToThis;
  }

  private void indexImplicitConversions() {
    ImmutableSet.Builder<Type> fromBuilder = ImmutableSet.builder();
    ImmutableSet.Builder<Type> toBuilder = ImmutableSet.builder();

    for (MethodNameDeclaration method : scope.getMethodDeclarations()) {
      if (method.getMethodKind() == MethodKind.OPERATOR
          && method.getName().equalsIgnoreCase("Implicit")
          && method.getParametersCount() == 1) {
        Type returnType = method.getReturnType();
        Type parameterType = method.getParameter(0).getType();
        if (returnType.is(this)) {
          toBuilder.add(parameterType);
        } else if (parameterType.is(this)) {
          fromBuilder.add(returnType);
        }
      }
    }

    typesWithImplicitConversionsToThis = toBuilder.build();
    typesWithImplicitConversionsFromThis = fromBuilder.build();
  }

  @Override
  public boolean canBeSpecialized(TypeSpecializationContext context) {
    for (ImagePart part : imageParts) {
      for (Type parameter : part.getTypeParameters()) {
        if (parameter.isTypeParameter() && context.getArgument(parameter) != null) {
          return true;
        }

        if (parameter.canBeSpecialized(context)) {
          return true;
        }
      }
    }

    return parents.stream()
        .filter(StructTypeImpl.class::isInstance)
        .map(StructTypeImpl.class::cast)
        .anyMatch(parent -> parent.canBeSpecialized(context));
  }

  @Override
  public GenerifiableTypeImpl doSpecialization(TypeSpecializationContext context) {
    return new StructTypeImpl(
        imageParts.stream()
            .map(imagePart -> imagePart.specialize(context))
            .collect(Collectors.toUnmodifiableList()),
        size,
        scope,
        parents,
        kind);
  }

  @Override
  protected final void doAfterSpecialization(TypeSpecializationContext context) {
    this.setParents(
        parents.stream()
            .map(parent -> parent.specialize(context))
            .collect(Collectors.toUnmodifiableSet()));
    this.scope = TypeScopeImpl.specializedScope(scope, this, context);
  }

  /**
   * Returns a set of all default array properties that can be called on this type.
   *
   * @return set of default array property declarations
   */
  public Set<NameDeclaration> findDefaultArrayProperties() {
    Set<NameDeclaration> result = new HashSet<>();
    findDefaultArrayProperties(this, result);
    return result;
  }

  private static void findDefaultArrayProperties(StructType type, Set<NameDeclaration> result) {
    type.typeScope().getPropertyDeclarations().stream()
        .filter(PropertyNameDeclaration::isArrayProperty)
        .filter(PropertyNameDeclaration::isDefaultProperty)
        .filter(
            declaration ->
                result.stream()
                    .map(PropertyNameDeclaration.class::cast)
                    .noneMatch(property -> property.hasSameParameterTypes(declaration)))
        .forEach(result::add);

    Type superType = type.superType();
    if (superType.isStruct()) {
      findDefaultArrayProperties((StructType) superType, result);
    }
  }

  static final class ImagePart {
    private final String name;
    private final List<Type> typeParameters;

    ImagePart(String name) {
      this(name, Collections.emptyList());
    }

    ImagePart(String name, List<Type> typeParameters) {
      this.name = name;
      this.typeParameters = typeParameters;
    }

    public List<Type> getTypeParameters() {
      return typeParameters;
    }

    private ImagePart specialize(TypeSpecializationContext context) {
      if (typeParameters.isEmpty()) {
        return this;
      }
      return new ImagePart(
          name,
          typeParameters.stream()
              .map(type -> type.specialize(context))
              .collect(Collectors.toUnmodifiableList()));
    }

    @Override
    public String toString() {
      if (!typeParameters.isEmpty()) {
        return name
            + "<"
            + typeParameters.stream().map(Type::getImage).collect(Collectors.joining(","))
            + ">";
      }
      return name;
    }
  }
}
