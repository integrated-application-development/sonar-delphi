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
package org.sonar.plugins.delphi.symbol.declaration;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.EnumElementNode;
import org.sonar.plugins.delphi.type.Type;

public class EnumElementNameDeclaration extends AbstractDelphiNameDeclaration
    implements TypedDeclaration {
  private final Type type;

  public EnumElementNameDeclaration(EnumElementNode node) {
    super(node.getNameDeclarationNode());
    this.type = node.getType();
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o) && type.is(((EnumElementNameDeclaration) o).type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), type.getImage().toLowerCase());
  }

  @Override
  public int compareTo(@NotNull DelphiNameDeclaration other) {
    int result = super.compareTo(other);
    if (result == 0) {
      EnumElementNameDeclaration that = (EnumElementNameDeclaration) other;
      result = type.getImage().compareTo(that.type.getImage());
    }
    return result;
  }

  @Override
  public String toString() {
    return "Enum element: image = '"
        + getNode().getImage()
        + "', line = "
        + getNode().getBeginLine()
        + " <"
        + getNode().getUnitName()
        + ">";
  }
}
