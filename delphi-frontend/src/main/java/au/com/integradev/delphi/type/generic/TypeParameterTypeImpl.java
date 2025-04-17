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
package au.com.integradev.delphi.type.generic;

import au.com.integradev.delphi.type.TypeImpl;
import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.type.Constraint;
import org.sonar.plugins.communitydelphi.api.type.Constraint.ClassConstraint;
import org.sonar.plugins.communitydelphi.api.type.Constraint.ConstructorConstraint;
import org.sonar.plugins.communitydelphi.api.type.Constraint.RecordConstraint;
import org.sonar.plugins.communitydelphi.api.type.Constraint.TypeConstraint;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.TypeParameterType;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

public final class TypeParameterTypeImpl extends TypeImpl implements TypeParameterType {
  private final String image;
  private List<Constraint> constraintItems;

  private TypeParameterTypeImpl(String image, List<Constraint> constraintItems) {
    this.image = image;
    this.constraintItems = ImmutableList.copyOf(constraintItems);
  }

  public static TypeParameterType create(String image, List<Constraint> constraintItems) {
    return new TypeParameterTypeImpl(image, constraintItems);
  }

  public static TypeParameterType create(String image) {
    return create(image, Collections.emptyList());
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public int size() {
    // meta type
    return 0;
  }

  @SuppressWarnings("removal")
  @Override
  public List<Type> constraints() {
    return constraintItems.stream()
        .filter(TypeConstraint.class::isInstance)
        .map(TypeConstraint.class::cast)
        .map(TypeConstraint::type)
        .collect(Collectors.toUnmodifiableList());
  }

  @Override
  public List<Constraint> constraintItems() {
    return constraintItems;
  }

  @Override
  public boolean isTypeParameter() {
    return true;
  }

  @Override
  public boolean canBeSpecialized(TypeSpecializationContext context) {
    return context.getArgument(this) != null;
  }

  @Override
  public Type specialize(TypeSpecializationContext context) {
    Type type = context.getArgument(this);
    return Objects.requireNonNullElse(type, this);
  }

  /**
   * Adds information from the full type parameter declaration.
   *
   * @param fullType Type representing the full type declaration
   */
  public void setFullType(TypeParameterType fullType) {
    this.constraintItems = fullType.constraintItems();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder(image);

    for (int i = 0; i < constraintItems.size(); i++) {
      builder.append((i == 0) ? ": " : ", ");

      Constraint constraint = constraintItems.get(i);
      if (constraint instanceof TypeConstraint) {
        builder.append(((TypeConstraint) constraint).type().getImage());
      } else if (constraint instanceof ClassConstraint) {
        builder.append("class");
      } else if (constraint instanceof ConstructorConstraint) {
        builder.append("constructor");
      } else if (constraint instanceof RecordConstraint) {
        builder.append("record");
      }
    }

    return builder.toString();
  }
}
