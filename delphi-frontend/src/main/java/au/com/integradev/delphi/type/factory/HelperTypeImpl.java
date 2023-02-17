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

import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.StructKind;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.HelperType;

public final class HelperTypeImpl extends StructTypeImpl implements HelperType {
  private final Type extendedType;

  HelperTypeImpl(
      List<ImagePart> imageParts,
      int size,
      DelphiScope scope,
      Set<Type> parents,
      Type extendedType,
      StructKind kind) {
    super(imageParts, size, scope, parents, kind);
    this.extendedType = extendedType;
  }

  @Override
  @NotNull
  public Type extendedType() {
    return extendedType;
  }

  @Override
  public boolean isForwardType() {
    return false;
  }

  @Override
  public boolean isHelper() {
    return true;
  }
}
