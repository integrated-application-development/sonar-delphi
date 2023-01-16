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

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.communitydelphi.type.Type;
import org.sonar.plugins.communitydelphi.type.Type.TypeParameterType;
import org.sonar.plugins.communitydelphi.type.Typed;
import org.sonar.plugins.communitydelphi.type.generic.DelphiTypeParameterType;

public final class GenericDefinitionNode extends DelphiNode {
  private String image;
  private List<TypeParameter> typeParameters;

  public GenericDefinitionNode(Token token) {
    super(token);
  }

  public GenericDefinitionNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<TypeParameter> getTypeParameters() {
    if (typeParameters == null) {
      ImmutableList.Builder<TypeParameter> builder = ImmutableList.builder();

      for (TypeParameterNode parameterNode : getTypeParameterNodes()) {
        List<Type> constraints =
            parameterNode.getTypeConstraintNodes().stream()
                .map(Typed::getType)
                .collect(Collectors.toUnmodifiableList());

        for (NameDeclarationNode name : parameterNode.getTypeParameterNameNodes()) {
          TypeParameterType type = DelphiTypeParameterType.create(name.getImage(), constraints);
          TypeParameter typeParameter = new TypeParameter(name, type);
          builder.add(typeParameter);
        }
      }

      typeParameters = builder.build();
    }

    return typeParameters;
  }

  public List<TypeParameterNode> getTypeParameterNodes() {
    return findChildrenOfType(TypeParameterNode.class);
  }

  @Override
  public String getImage() {
    if (image == null) {
      image =
          "<"
              + getTypeParameters().stream()
                  .map(TypeParameter::getLocation)
                  .map(DelphiNode::getImage)
                  .collect(Collectors.joining())
              + ">";
    }
    return image;
  }

  public static class TypeParameter {
    private final NameDeclarationNode location;
    private final TypeParameterType type;

    private TypeParameter(NameDeclarationNode location, TypeParameterType type) {
      this.location = location;
      this.type = type;
    }

    public NameDeclarationNode getLocation() {
      return location;
    }

    public TypeParameterType getType() {
      return type;
    }
  }
}
