package org.sonar.plugins.delphi.antlr.ast.node;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.TypeParameterType;
import org.sonar.plugins.delphi.type.Typed;
import org.sonar.plugins.delphi.type.generic.DelphiTypeParameterType;

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
