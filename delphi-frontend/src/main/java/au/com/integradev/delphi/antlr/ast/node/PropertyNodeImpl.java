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
import java.util.Collections;
import java.util.List;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.CustomAttributeListNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterListNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyReadSpecifierNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyWriteSpecifierNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.ast.VisibilitySectionNode;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public final class PropertyNodeImpl extends DelphiNodeImpl implements PropertyNode {
  private VisibilityType visibility;
  private Type type;

  public PropertyNodeImpl(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public VisibilityType getVisibility() {
    if (visibility == null) {
      visibility = ((VisibilitySectionNode) getParent()).getVisibility();
    }
    return visibility;
  }

  @Override
  public Type getType() {
    if (type == null) {
      TypeNode typeNode = getTypeNode();
      type = (typeNode == null) ? TypeFactory.unknownType() : typeNode.getType();
    }
    return type;
  }

  @Override
  public NameDeclarationNode getPropertyName() {
    return (NameDeclarationNode) getChild(0);
  }

  @Override
  public TypeNode getTypeNode() {
    return getFirstChildOfType(TypeNode.class);
  }

  @Override
  @Nullable
  public FormalParameterListNode getParameterListNode() {
    DelphiNode node = getChild(1);
    return (node instanceof FormalParameterListNode) ? (FormalParameterListNode) node : null;
  }

  @Override
  @Nullable
  public PropertyReadSpecifierNode getReadSpecifier() {
    return getFirstChildOfType(PropertyReadSpecifierNode.class);
  }

  @Override
  @Nullable
  public PropertyWriteSpecifierNode getWriteSpecifier() {
    return getFirstChildOfType(PropertyWriteSpecifierNode.class);
  }

  @Override
  public List<FormalParameterData> getParameters() {
    FormalParameterListNode paramList = getParameterListNode();
    return (paramList == null) ? Collections.emptyList() : paramList.getParameters();
  }

  @Override
  public List<Type> getParameterTypes() {
    FormalParameterListNode paramList = getParameterListNode();
    return (paramList == null) ? Collections.emptyList() : paramList.getParameterTypes();
  }

  @Override
  public CustomAttributeListNode getAttributeList() {
    return getFirstChildOfType(CustomAttributeListNode.class);
  }

  @Override
  public boolean isClassProperty() {
    return getFirstChildWithTokenType(DelphiTokenType.CLASS) != null;
  }

  @Override
  public boolean isDefaultProperty() {
    return getFirstChildWithTokenType(DelphiTokenType.DEFAULT) != null;
  }
}
