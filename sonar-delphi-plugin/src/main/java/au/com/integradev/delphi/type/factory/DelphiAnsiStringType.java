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

import au.com.integradev.delphi.type.Type.AnsiStringType;
import au.com.integradev.delphi.type.intrinsic.IntrinsicType;

class DelphiAnsiStringType extends DelphiStringType implements AnsiStringType {
  private final int codePage;

  DelphiAnsiStringType(int size, CharacterType characterType, int codePage) {
    super(null, size, characterType);
    this.codePage = codePage;
  }

  @Override
  public String getImage() {
    String image = IntrinsicType.ANSISTRING.fullyQualifiedName();
    if (codePage != 0) {
      image += "(" + codePage + ")";
    }
    return image;
  }

  @Override
  public boolean isAnsiString() {
    return true;
  }

  @Override
  public int codePage() {
    return codePage;
  }
}
