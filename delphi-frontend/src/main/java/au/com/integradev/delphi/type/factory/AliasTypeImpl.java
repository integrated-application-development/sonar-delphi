/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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

import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.AliasType;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

public abstract class AliasTypeImpl<T extends Type> implements AliasType {
  private final String aliasImage;
  private final T aliasedType;
  private final boolean strong;

  protected AliasTypeImpl(String aliasImage, T aliasedType, boolean strong) {
    this.aliasImage = aliasImage;
    this.aliasedType = aliasedType;
    this.strong = strong;
  }

  @Override
  public String aliasImage() {
    return aliasImage;
  }

  @Override
  public T aliasedType() {
    return aliasedType;
  }

  @Override
  public String getImage() {
    return strong ? aliasImage : aliasedType.getImage();
  }

  @Override
  public boolean is(String image) {
    return aliasImage.equalsIgnoreCase(image) || (!strong && aliasedType.is(image));
  }

  @Override
  public boolean is(Type type) {
    return is(type.getImage());
  }

  @Override
  public boolean is(IntrinsicType intrinsic) {
    return is(intrinsic.fullyQualifiedName());
  }

  @Override
  public boolean isAlias() {
    return true;
  }

  @Override
  public boolean isWeakAlias() {
    return !strong;
  }

  @Override
  public boolean isStrongAlias() {
    return strong;
  }

  @Override
  public Type specialize(TypeSpecializationContext context) {
    return this;
  }
}
