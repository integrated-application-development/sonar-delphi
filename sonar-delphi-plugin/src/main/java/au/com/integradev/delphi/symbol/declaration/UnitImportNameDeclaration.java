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
package au.com.integradev.delphi.symbol.declaration;

import au.com.integradev.delphi.antlr.ast.node.UnitImportNode;
import au.com.integradev.delphi.symbol.scope.FileScope;
import java.util.Objects;
import javax.annotation.Nullable;

public final class UnitImportNameDeclaration extends QualifiedDelphiNameDeclaration {
  private final UnitNameDeclaration originalDeclaration;

  public UnitImportNameDeclaration(
      UnitImportNode node, @Nullable UnitNameDeclaration originalDeclaration) {
    super(node.getNameNode());
    this.originalDeclaration = originalDeclaration;
  }

  @Nullable
  public UnitNameDeclaration getOriginalDeclaration() {
    return originalDeclaration;
  }

  @Nullable
  public FileScope getUnitScope() {
    if (originalDeclaration == null) {
      return null;
    }
    return originalDeclaration.getFileScope();
  }

  @Override
  public boolean equals(Object other) {
    if (super.equals(other)) {
      UnitImportNameDeclaration that = (UnitImportNameDeclaration) other;
      return Objects.equals(originalDeclaration, that.originalDeclaration);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), originalDeclaration);
  }

  @Override
  public String toString() {
    return "Import " + getName();
  }
}
