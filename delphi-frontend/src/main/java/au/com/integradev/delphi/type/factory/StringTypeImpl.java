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
import org.sonar.plugins.communitydelphi.api.type.Type.StringType;

public class StringTypeImpl extends TypeImpl implements StringType {
  private final String image;
  private final int size;
  private final CharacterType characterType;

  StringTypeImpl(String image, int size, CharacterType characterType) {
    this.image = image;
    this.size = size;
    this.characterType = characterType;
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public CharacterType characterType() {
    return characterType;
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isString() {
    return true;
  }
}
