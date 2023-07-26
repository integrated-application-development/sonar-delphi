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

import static org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope.unknownScope;

import au.com.integradev.delphi.type.TypeImpl;
import java.util.Objects;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ClassReferenceType;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;

public final class ClassReferenceTypeImpl extends TypeImpl
    implements ClassReferenceType, ScopedType {
  private final String image;
  private Type classType;
  private final int size;

  ClassReferenceTypeImpl(@Nullable String image, Type classType, int size) {
    this.image = image;
    this.classType = classType;
    this.size = size;
  }

  @Override
  public String getImage() {
    return Objects.requireNonNullElse(image, "class of " + classType.getImage());
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public boolean isClassReference() {
    return true;
  }

  @Override
  public DelphiScope typeScope() {
    return classType instanceof ScopedType ? ((ScopedType) classType).typeScope() : unknownScope();
  }

  @Override
  public Type classType() {
    return classType;
  }

  /**
   * Sets the classOf type. Used for type completion at the end of a type section.
   *
   * @param type The classOf type
   */
  public void setClassType(Type type) {
    this.classType = type;
  }
}
