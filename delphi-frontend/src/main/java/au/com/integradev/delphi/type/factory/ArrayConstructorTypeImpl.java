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
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ArrayConstructorType;

public final class ArrayConstructorTypeImpl extends TypeImpl implements ArrayConstructorType {
  private final ImmutableList<Type> elementTypes;

  ArrayConstructorTypeImpl(List<Type> elementTypes) {
    this.elementTypes = ImmutableList.copyOf(elementTypes);
  }

  @Override
  public String getImage() {
    return "[" + elementTypes.stream().map(Type::getImage).collect(Collectors.joining(",")) + "]";
  }

  @Override
  public int size() {
    // meta type
    return 0;
  }

  @Override
  public List<Type> elementTypes() {
    return elementTypes;
  }

  @Override
  public boolean isEmpty() {
    return elementTypes.isEmpty();
  }

  @Override
  public boolean isArrayConstructor() {
    return true;
  }
}
