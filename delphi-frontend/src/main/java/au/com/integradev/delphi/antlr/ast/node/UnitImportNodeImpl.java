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
package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.QualifiedNameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;

public final class UnitImportNodeImpl extends DelphiNodeImpl implements UnitImportNode {
  public UnitImportNodeImpl(Token token) {
    super(token);
  }

  public UnitImportNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public QualifiedNameDeclarationNode getNameNode() {
    return (QualifiedNameDeclarationNode) getChild(0);
  }

  @Override
  public boolean isResolvedImport() {
    UnitImportNameDeclaration declaration = getImportNameDeclaration();
    return declaration != null && declaration.getOriginalDeclaration() != null;
  }

  @Override
  public UnitImportNameDeclaration getImportNameDeclaration() {
    return (UnitImportNameDeclaration) getNameNode().getNameDeclaration();
  }
}
