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
import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Objects;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.GenericDefinitionNode;
import org.sonar.plugins.communitydelphi.api.ast.IdentifierNode;
import org.sonar.plugins.communitydelphi.api.ast.SimpleNameDeclarationNode;

public final class SimpleNameDeclarationNodeImpl extends NameDeclarationNodeImpl
    implements SimpleNameDeclarationNode {
  private static final Map<Class<?>, DeclarationKind> PARENT_NODE_KIND_MAP =
      ImmutableMap.<Class<?>, DeclarationKind>builder()
          .put(ConstDeclarationNodeImpl.class, DeclarationKind.CONST)
          .put(ConstStatementNodeImpl.class, DeclarationKind.INLINE_CONST)
          .put(EnumElementNodeImpl.class, DeclarationKind.ENUM_ELEMENT)
          .put(ExceptItemNodeImpl.class, DeclarationKind.EXCEPT_ITEM)
          .put(LabelDeclarationNodeImpl.class, DeclarationKind.LABEL)
          .put(ForLoopVarDeclarationNodeImpl.class, DeclarationKind.LOOP_VAR)
          .put(RoutineNameNodeImpl.class, DeclarationKind.ROUTINE)
          .put(PropertyNodeImpl.class, DeclarationKind.PROPERTY)
          .put(RecordVariantTagNodeImpl.class, DeclarationKind.RECORD_VARIANT_TAG)
          .put(TypeDeclarationNodeImpl.class, DeclarationKind.TYPE)
          .put(TypeParameterNodeImpl.class, DeclarationKind.TYPE_PARAMETER)
          .build();

  private static final Map<Class<?>, DeclarationKind> GRANDPARENT_NODE_KIND_MAP =
      Map.of(
          VarDeclarationNodeImpl.class, DeclarationKind.VAR,
          FieldDeclarationNodeImpl.class, DeclarationKind.FIELD,
          FormalParameterNodeImpl.class, DeclarationKind.PARAMETER,
          VarStatementNodeImpl.class, DeclarationKind.INLINE_VAR);

  private String image;

  public SimpleNameDeclarationNodeImpl(Token token) {
    super(token);
  }

  public SimpleNameDeclarationNodeImpl(int tokenType) {
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

  @Override
  public IdentifierNode getIdentifier() {
    return (IdentifierNode) getChild(0);
  }

  @Override
  public DeclarationKind getKind() {
    DeclarationKind kind = PARENT_NODE_KIND_MAP.get(parent.getClass());
    if (kind == null) {
      kind = GRANDPARENT_NODE_KIND_MAP.get(getNthParent(2).getClass());
    }
    return Objects.requireNonNull(kind, "Unhandled DeclarationKind");
  }
}
