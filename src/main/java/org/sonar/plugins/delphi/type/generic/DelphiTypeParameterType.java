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
package org.sonar.plugins.delphi.type.generic;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.TypeParameterType;

public class DelphiTypeParameterType extends DelphiType implements TypeParameterType {
  private final String image;
  private List<Type> constraints;

  private DelphiTypeParameterType(String image, List<Type> constraints) {
    this.image = image;
    this.constraints = ImmutableList.copyOf(constraints);
  }

  public static TypeParameterType create(String image, List<Type> constraints) {
    return new DelphiTypeParameterType(image, constraints);
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

  @Override
  public List<Type> constraints() {
    return constraints;
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

  @Override
  public void setFullType(TypeParameterType fullType) {
    this.constraints = fullType.constraints();
  }
}
