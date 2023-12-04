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
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.AttributeGroupNode;
import org.sonar.plugins.communitydelphi.api.ast.AttributeListNode;
import org.sonar.plugins.communitydelphi.api.ast.AttributeNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public final class AttributeListNodeImpl extends DelphiNodeImpl implements AttributeListNode {
  public AttributeListNodeImpl(Token token) {
    super(token);
  }

  public AttributeListNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public List<AttributeGroupNode> getAttributeGroups() {
    return findChildrenOfType(AttributeGroupNode.class);
  }

  @Override
  public List<AttributeNode> getAttributes() {
    return findDescendantsOfType(AttributeNode.class);
  }

  @Override
  public List<Type> getAttributeTypes() {
    return getAttributes().stream()
        .map(AttributeNode::getTypeNameOccurrence)
        .map(
            occurrence -> {
              if (occurrence == null) {
                return TypeFactory.unknownType();
              }

              NameDeclaration declaration = occurrence.getNameDeclaration();
              if (!(declaration instanceof TypeNameDeclaration)) {
                return TypeFactory.unknownType();
              }

              return ((TypeNameDeclaration) declaration).getType();
            })
        .collect(Collectors.toList());
  }
}
