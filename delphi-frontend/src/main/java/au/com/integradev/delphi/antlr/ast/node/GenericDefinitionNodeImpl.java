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
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.TypeParameterType;
import org.sonar.plugins.communitydelphi.api.type.Typed;
import au.com.integradev.delphi.type.generic.TypeParameterTypeImpl;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.GenericDefinitionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeParameterNode;

public final class GenericDefinitionNodeImpl extends DelphiNodeImpl
    implements GenericDefinitionNode {
  private String image;
  private List<TypeParameter> typeParameters;

  public GenericDefinitionNodeImpl(Token token) {
    super(token);
  }

  public GenericDefinitionNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public List<TypeParameter> getTypeParameters() {
    if (typeParameters == null) {
      ImmutableList.Builder<TypeParameter> builder = ImmutableList.builder();

      for (TypeParameterNode parameterNode : getTypeParameterNodes()) {
        List<Type> constraints =
            parameterNode.getTypeConstraintNodes().stream()
                .map(Typed::getType)
                .collect(Collectors.toUnmodifiableList());

        for (NameDeclarationNode name : parameterNode.getTypeParameterNameNodes()) {
          TypeParameterType type = TypeParameterTypeImpl.create(name.getImage(), constraints);
          TypeParameter typeParameter = new TypeParameterImpl(name, type);
          builder.add(typeParameter);
        }
      }

      typeParameters = builder.build();
    }

    return typeParameters;
  }

  @Override
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

  private static final class TypeParameterImpl implements TypeParameter {
    private final NameDeclarationNode location;
    private final TypeParameterType type;

    private TypeParameterImpl(NameDeclarationNode location, TypeParameterType type) {
      this.location = location;
      this.type = type;
    }

    @Override
    public NameDeclarationNode getLocation() {
      return location;
    }

    @Override
    public TypeParameterType getType() {
      return type;
    }
  }
}
