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
package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationListNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.ast.VarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VarSectionNode;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class VarDeclarationNodeImpl extends DelphiNodeImpl implements VarDeclarationNode {
  public VarDeclarationNodeImpl(Token token) {
    super(token);
  }

  public VarDeclarationNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public NameDeclarationListNode getNameDeclarationList() {
    return (NameDeclarationListNode) jjtGetChild(0);
  }

  @Override
  public VarSectionNode getVarSection() {
    return (VarSectionNode) jjtGetParent();
  }

  @Override
  public TypeNode getTypeNode() {
    return (TypeNode) jjtGetChild(1);
  }

  @Override
  public boolean isAbsolute() {
    return getFirstChildWithTokenType(DelphiTokenType.ABSOLUTE) != null;
  }

  @NotNull
  @Override
  public Type getType() {
    return getTypeNode().getType();
  }
}
