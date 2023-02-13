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
import java.util.List;
import org.antlr.runtime.Token;

public final class GenericArgumentsNodeImpl extends AbstractDelphiNode
    implements GenericArgumentsNode {
  private String image;

  public GenericArgumentsNodeImpl(Token token) {
    super(token);
  }

  public GenericArgumentsNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public List<TypeNode> getTypeArguments() {
    return findChildrenOfType(TypeNode.class);
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = buildImage();
    }
    return image;
  }

  private String buildImage() {
    StringBuilder builder = new StringBuilder();
    builder.append("<");

    for (TypeNode typeNode : getTypeArguments()) {
      if (builder.length() > 1) {
        builder.append(",");
      }

      if (typeNode instanceof TypeReferenceNode) {
        TypeReferenceNode typeReference = (TypeReferenceNode) typeNode;
        if (typeReference.getTypeDeclaration() != null) {
          builder.append(typeReference.getImage());
        } else {
          builder.append(typeReference.getNameNode().getImage());
        }
      } else {
        builder.append(typeNode.getImage());
      }
    }

    builder.append(">");

    return builder.toString();
  }
}
