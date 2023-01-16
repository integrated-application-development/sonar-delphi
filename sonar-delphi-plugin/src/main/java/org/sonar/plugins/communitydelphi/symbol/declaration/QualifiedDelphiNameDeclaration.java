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
package org.sonar.plugins.communitydelphi.symbol.declaration;

import java.util.Objects;
import org.sonar.plugins.communitydelphi.antlr.ast.node.QualifiedNameDeclarationNode;
import org.sonar.plugins.communitydelphi.symbol.Qualifiable;
import org.sonar.plugins.communitydelphi.symbol.QualifiedName;
import org.sonar.plugins.communitydelphi.symbol.scope.DelphiScope;

public abstract class QualifiedDelphiNameDeclaration extends AbstractDelphiNameDeclaration
    implements Qualifiable {
  private final QualifiedName qualifiedName;

  protected QualifiedDelphiNameDeclaration(QualifiedNameDeclarationNode node) {
    super(node);
    qualifiedName = node.getQualifiedName();
  }

  protected QualifiedDelphiNameDeclaration(QualifiedNameDeclarationNode node, DelphiScope scope) {
    super(node, scope);
    qualifiedName = node.getQualifiedName();
  }

  @Override
  public QualifiedName getQualifiedName() {
    return qualifiedName;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o)
        && getQualifiedNameParts()
            .equals(((QualifiedDelphiNameDeclaration) o).getQualifiedNameParts());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), qualifiedName.parts());
  }
}
