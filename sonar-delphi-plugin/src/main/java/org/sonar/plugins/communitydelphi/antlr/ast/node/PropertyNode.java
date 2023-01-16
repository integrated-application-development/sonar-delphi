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
package org.sonar.plugins.communitydelphi.antlr.ast.node;

import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.ast.Node;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.communitydelphi.antlr.DelphiLexer;
import org.sonar.plugins.communitydelphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.communitydelphi.type.DelphiType;
import org.sonar.plugins.communitydelphi.type.Type;
import org.sonar.plugins.communitydelphi.type.Typed;

public final class PropertyNode extends DelphiNode implements Typed, Visibility {
  private VisibilityType visibility;
  private Type type;

  public PropertyNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public VisibilityType getVisibility() {
    if (visibility == null) {
      visibility = ((VisibilitySectionNode) jjtGetParent()).getVisibility();
    }
    return visibility;
  }

  @Override
  @NotNull
  public Type getType() {
    if (type == null) {
      TypeNode typeNode = getTypeNode();
      type = (typeNode == null) ? DelphiType.unknownType() : typeNode.getType();
    }
    return type;
  }

  public NameDeclarationNode getPropertyName() {
    return (NameDeclarationNode) jjtGetChild(0);
  }

  public TypeNode getTypeNode() {
    return getFirstChildOfType(TypeNode.class);
  }

  @Nullable
  public FormalParameterListNode getParameterListNode() {
    Node node = jjtGetChild(1);
    return (node instanceof FormalParameterListNode) ? (FormalParameterListNode) node : null;
  }

  @Nullable
  public PropertyReadSpecifierNode getReadSpecifier() {
    return getFirstChildOfType(PropertyReadSpecifierNode.class);
  }

  @Nullable
  public PropertyWriteSpecifierNode getWriteSpecifier() {
    return getFirstChildOfType(PropertyWriteSpecifierNode.class);
  }

  public List<FormalParameterData> getParameters() {
    FormalParameterListNode paramList = getParameterListNode();
    return (paramList == null) ? Collections.emptyList() : paramList.getParameters();
  }

  public List<Type> getParameterTypes() {
    FormalParameterListNode paramList = getParameterListNode();
    return (paramList == null) ? Collections.emptyList() : paramList.getParameterTypes();
  }

  public boolean isClassProperty() {
    return getFirstChildWithId(DelphiLexer.CLASS) != null;
  }

  public boolean isDefaultProperty() {
    return getFirstChildWithId(DelphiLexer.DEFAULT) != null;
  }
}
