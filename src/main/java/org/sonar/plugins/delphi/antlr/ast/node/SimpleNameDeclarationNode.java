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
package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.Map;
import java.util.Objects;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class SimpleNameDeclarationNode extends NameDeclarationNode {
  private static final Map<Class<?>, DeclarationKind> PARENT_NODE_KIND_MAP =
      Map.of(
          ConstDeclarationNode.class, DeclarationKind.CONST,
          ConstStatementNode.class, DeclarationKind.INLINE_CONST,
          EnumElementNode.class, DeclarationKind.ENUM_ELEMENT,
          ExceptItemNode.class, DeclarationKind.EXCEPT_ITEM,
          ForLoopVarDeclarationNode.class, DeclarationKind.LOOP_VAR,
          MethodNameNode.class, DeclarationKind.METHOD,
          PropertyNode.class, DeclarationKind.PROPERTY,
          RecordVariantTagNode.class, DeclarationKind.RECORD_VARIANT_TAG,
          TypeDeclarationNode.class, DeclarationKind.TYPE,
          TypeParameterNode.class, DeclarationKind.TYPE_PARAMETER);

  private static final Map<Class<?>, DeclarationKind> GRANDPARENT_NODE_KIND_MAP =
      Map.of(
          VarDeclarationNode.class, DeclarationKind.VAR,
          FieldDeclarationNode.class, DeclarationKind.FIELD,
          FormalParameterNode.class, DeclarationKind.PARAMETER,
          VarStatementNode.class, DeclarationKind.INLINE_VAR);

  private String image;

  public SimpleNameDeclarationNode(Token token) {
    super(token);
  }

  public SimpleNameDeclarationNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public String getImage() {
    if (image == null) {
      GenericDefinitionNode genericDefinition = getGenericDefinition();
      StringBuilder builder = new StringBuilder();
      builder.append(getIdentifier().getImage());
      if (genericDefinition != null) {
        builder.append(genericDefinition.getImage());
      }
      image = builder.toString();
    }
    return image;
  }

  public IdentifierNode getIdentifier() {
    return (IdentifierNode) jjtGetChild(0);
  }

  @NotNull
  @Override
  public DeclarationKind getKind() {
    DeclarationKind kind = PARENT_NODE_KIND_MAP.get(parent.getClass());
    if (kind == null) {
      kind = GRANDPARENT_NODE_KIND_MAP.get(getNthParent(2).getClass());
    }
    return Objects.requireNonNull(kind, "Unhandled DeclarationKind");
  }
}
