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
package org.sonar.plugins.delphi.type.factory;

import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.TypeType;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

class DelphiTypeType extends DelphiType implements TypeType {
  private final String image;
  private final Type originalType;

  DelphiTypeType(String image, Type originalType) {
    this.image = image;
    this.originalType = originalType;
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public int size() {
    return originalType.size();
  }

  @Override
  public Type originalType() {
    return originalType;
  }

  @Override
  public boolean isTypeType() {
    return true;
  }

  @Override
  public Type specialize(TypeSpecializationContext context) {
    if (originalType.isTypeParameter()) {
      return new DelphiTypeType(getImage(), originalType.specialize(context));
    }
    return this;
  }
}
