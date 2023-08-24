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
package au.com.integradev.delphi.symbol.declaration;

import au.com.integradev.delphi.symbol.SymbolicNode;
import java.util.Objects;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeParameterNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.TypeParameterType;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

public class TypeParameterNameDeclarationImpl extends NameDeclarationImpl
    implements TypeParameterNameDeclaration {
  private final Type type;

  public TypeParameterNameDeclarationImpl(DelphiNode location, TypeParameterType type) {
    this(new SymbolicNode(location), type);
  }

  private TypeParameterNameDeclarationImpl(SymbolicNode location, Type type) {
    super(location);
    this.type = type;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Override
  public NameDeclaration doSpecialization(TypeSpecializationContext context) {
    Type specialized = type.specialize(context);
    if (type.isTypeParameter() && specialized != type) {
      return new TypeParameterNameDeclarationImpl(node, specialized);
    }
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (super.equals(o)) {
      TypeParameterNameDeclarationImpl that = (TypeParameterNameDeclarationImpl) o;
      return type == that.type;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), type);
  }

  @Override
  public String toString() {
    return "type parameter <" + type.getImage() + ">";
  }
}
