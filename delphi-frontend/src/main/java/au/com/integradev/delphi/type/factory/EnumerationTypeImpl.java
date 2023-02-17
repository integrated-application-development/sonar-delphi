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

import au.com.integradev.delphi.type.TypeImpl;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.Type.EnumType;

public final class EnumerationTypeImpl extends TypeImpl implements EnumType {
  private final String image;
  private final DelphiScope scope;

  EnumerationTypeImpl(String image, DelphiScope scope) {
    this.image = image;
    this.scope = scope;
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public int size() {
    // Assumes $MinEnumSize 1 and 256 elements or less.
    // See: https://www.oreilly.com/library/view/delphi-in-a/1565926595/re440.html
    return 1;
  }

  @Override
  @NotNull
  public DelphiScope typeScope() {
    return scope;
  }

  @Override
  public boolean isEnum() {
    return true;
  }
}
