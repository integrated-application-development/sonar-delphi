package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import org.sonar.plugins.communitydelphi.api.type.Type.TypeParameterType;

public interface GenericDefinitionNode extends DelphiNode {
  List<TypeParameter> getTypeParameters();

  List<TypeParameterNode> getTypeParameterNodes();

  interface TypeParameter {
    NameDeclarationNode getLocation();

    TypeParameterType getType();
  }
}
