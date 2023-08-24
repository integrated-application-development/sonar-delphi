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

import java.util.Objects;
import org.sonar.plugins.communitydelphi.api.ast.QualifiedNameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.symbol.QualifiedName;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.QualifiedNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;

public abstract class QualifiedNameDeclarationImpl extends NameDeclarationImpl
    implements QualifiedNameDeclaration {
  private final QualifiedName qualifiedName;

  protected QualifiedNameDeclarationImpl(QualifiedNameDeclarationNode node) {
    super(node);
    qualifiedName = node.getQualifiedName();
  }

  protected QualifiedNameDeclarationImpl(QualifiedNameDeclarationNode node, DelphiScope scope) {
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
        && getQualifiedNameParts().equals(((QualifiedNameDeclaration) o).getQualifiedNameParts());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), qualifiedName.parts());
  }
}
