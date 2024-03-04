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
package au.com.integradev.delphi.type.factory;

import static java.util.function.Predicate.not;

import au.com.integradev.delphi.symbol.scope.TypeScopeImpl;
import au.com.integradev.delphi.type.generic.GenerifiableTypeImpl;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.StructKind;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

public class StructTypeImpl extends GenerifiableTypeImpl implements StructType {
  private final List<ImagePart> imageParts;
  private final int size;
  private String image;
  private DelphiScope scope;
  private Set<Type> ancestorList;
  private StructKind kind;
  private Type parent;
  private List<Type> attributeTypes;

  StructTypeImpl(
      List<ImagePart> imageParts,
      int size,
      DelphiScope scope,
      Set<Type> ancestorList,
      StructKind kind,
      List<Type> attributeTypes) {
    this.imageParts = imageParts;
    this.size = size;
    this.scope = scope;
    this.kind = kind;
    this.attributeTypes = attributeTypes;
    setAncestors(ancestorList);
  }

  private void setAncestors(Set<Type> ancestorList) {
    this.ancestorList = Set.copyOf(ancestorList);
    if (isInterface()) {
      this.parent = Iterables.getFirst(ancestorList, TypeFactory.unknownType());
    } else {
      this.parent =
          this.ancestorList.stream()
              .filter(StructTypeImpl.class::isInstance)
              .filter(not(Type::isInterface))
              .findFirst()
              .orElse(TypeFactory.unknownType());
    }
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = imageParts.stream().map(ImagePart::toString).collect(Collectors.joining("."));
    }
    return image;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isDescendantOf(String image) {
    for (Type ancestor : ancestorList) {
      if (ancestor.is(image) || ancestor.isDescendantOf(image)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Type parent() {
    return parent;
  }

  @Override
  public Set<Type> ancestorList() {
    return ancestorList;
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
  public DelphiScope typeScope() {
    return scope;
  }

  @Override
  public StructKind kind() {
    return kind;
  }

  /**
   * Adds the full type declaration's information.
   *
   * @param fullType Type representing the full type declaration
   */
  public void setFullType(StructType fullType) {
    this.scope = fullType.typeScope();
    this.ancestorList = ImmutableSet.copyOf(fullType.ancestorList());
    this.kind = fullType.kind();
    this.parent = fullType.parent();
  }

  @Override
  public List<Type> attributeTypes() {
    return attributeTypes;
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

    return ancestorList.stream()
        .filter(StructTypeImpl.class::isInstance)
        .map(StructTypeImpl.class::cast)
        .anyMatch(ancestor -> ancestor.canBeSpecialized(context));
  }

  @Override
  public GenerifiableTypeImpl doSpecialization(TypeSpecializationContext context) {
    return new StructTypeImpl(
        imageParts.stream()
            .map(imagePart -> imagePart.specialize(context))
            .collect(Collectors.toUnmodifiableList()),
        size,
        scope,
        ancestorList,
        kind,
        attributeTypes);
  }

  @Override
  protected final void doAfterSpecialization(TypeSpecializationContext context) {
    this.setAncestors(
        ancestorList.stream()
            .map(ancestor -> ancestor.specialize(context))
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

    Type parent = type.parent();
    if (parent.isStruct()) {
      findDefaultArrayProperties((StructType) parent, result);
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
