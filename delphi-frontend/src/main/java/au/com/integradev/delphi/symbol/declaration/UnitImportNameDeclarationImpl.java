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

import static java.util.Comparator.naturalOrder;
import static java.util.Comparator.nullsLast;

import com.google.common.collect.ComparisonChain;
import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;

public final class UnitImportNameDeclarationImpl extends QualifiedNameDeclarationImpl
    implements UnitImportNameDeclaration {
  private final boolean alias;
  private final UnitNameDeclaration originalDeclaration;

  public UnitImportNameDeclarationImpl(
      UnitImportNode node, boolean alias, @Nullable UnitNameDeclaration originalDeclaration) {
    super(node.getNameNode());
    this.alias = alias;
    this.originalDeclaration = originalDeclaration;
  }

  @Override
  public boolean isAlias() {
    return alias;
  }

  @Override
  @Nullable
  public UnitNameDeclaration getOriginalDeclaration() {
    return originalDeclaration;
  }

  @Override
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
      UnitImportNameDeclarationImpl that = (UnitImportNameDeclarationImpl) other;
      return alias == that.alias && Objects.equals(originalDeclaration, that.originalDeclaration);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), alias, originalDeclaration);
  }

  @Override
  public int compareTo(@Nonnull NameDeclaration other) {
    int result = super.compareTo(other);
    if (result == 0) {
      UnitImportNameDeclarationImpl that = (UnitImportNameDeclarationImpl) other;
      return ComparisonChain.start()
          .compareTrueFirst(this.alias, that.alias)
          .compare(this.originalDeclaration, that.originalDeclaration, nullsLast(naturalOrder()))
          .result();
    }
    return result;
  }

  @Override
  public String toString() {
    return "Import " + getName();
  }
}
