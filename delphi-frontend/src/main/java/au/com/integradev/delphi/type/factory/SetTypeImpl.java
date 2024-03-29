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

import au.com.integradev.delphi.type.TypeImpl;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.CollectionType;

public final class SetTypeImpl extends TypeImpl implements CollectionType {
  private final Type elementType;

  SetTypeImpl(Type elementType) {
    this.elementType = elementType;
  }

  @Override
  public String getImage() {
    return "set of " + elementType().getImage();
  }

  @Override
  public int size() {
    // We're assuming the largest possible size here, but Delphi will actually try to store sets in
    // less bytes if possible.
    // See: https://stackoverflow.com/a/30338451
    return 32;
  }

  @Override
  public Type elementType() {
    return elementType;
  }

  @Override
  public boolean isSet() {
    return true;
  }
}
