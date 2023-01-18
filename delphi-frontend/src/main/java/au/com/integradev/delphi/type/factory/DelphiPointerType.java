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

import au.com.integradev.delphi.type.DelphiType;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Type.PointerType;
import au.com.integradev.delphi.type.generic.TypeSpecializationContext;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

class DelphiPointerType extends DelphiType implements PointerType {
  private final String image;
  private Type dereferencedType;
  private final int size;
  private boolean allowsPointerMath;

  DelphiPointerType(String image, Type dereferencedType, int size, boolean allowsPointerMath) {
    this.image = image;
    this.dereferencedType = dereferencedType;
    this.size = size;
    this.allowsPointerMath = allowsPointerMath;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public String getImage() {
    return Objects.requireNonNullElse(image, "^" + dereferencedType.getImage());
  }

  @Override
  @NotNull
  public Type dereferencedType() {
    return dereferencedType;
  }

  @Override
  public boolean allowsPointerMath() {
    return allowsPointerMath;
  }

  @Override
  public void setDereferencedType(Type type) {
    this.dereferencedType = type;
  }

  @Override
  public void setAllowsPointerMath() {
    this.allowsPointerMath = true;
  }

  @Override
  public boolean isPointer() {
    return true;
  }

  @Override
  public boolean isNilPointer() {
    return dereferencedType.isVoid();
  }

  @Override
  public boolean isUntypedPointer() {
    return dereferencedType.isUntyped();
  }

  @Override
  public Type specialize(TypeSpecializationContext context) {
    if (dereferencedType().isTypeParameter()) {
      return new DelphiPointerType(
          null, dereferencedType().specialize(context), size, allowsPointerMath);
    }
    return this;
  }
}
