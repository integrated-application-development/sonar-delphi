/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
import org.sonar.plugins.communitydelphi.api.ast.CustomAttributeGroupNode;
import org.sonar.plugins.communitydelphi.api.ast.CustomAttributeNode;

public final class CustomAttributeGroupNodeImpl extends DelphiNodeImpl
    implements CustomAttributeGroupNode {
  public CustomAttributeGroupNodeImpl(Token token) {
    super(token);
  }

  public CustomAttributeGroupNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public List<CustomAttributeNode> getAttributes() {
    return findChildrenOfType(CustomAttributeNode.class);
  }

  @Override
  public String getImage() {
    return getAttributes().stream()
        .map(attribute -> attribute.getNameReference().fullyQualifiedName())
        .collect(Collectors.joining(", ", "[", "]"));
  }
}
