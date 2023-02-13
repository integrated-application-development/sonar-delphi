package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.type.Type.TypeParameterType;
import java.util.List;

public interface GenericDefinitionNode extends DelphiNode {
  List<TypeParameter> getTypeParameters();

  List<TypeParameterNode> getTypeParameterNodes();

  interface TypeParameter {
    NameDeclarationNode getLocation();

    TypeParameterType getType();
  }
}
