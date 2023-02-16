package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.type.Type.TypeParameterType;
import java.util.List;

public interface GenericDefinitionNode extends DelphiNode {
  List<TypeParameter> getTypeParameters();

  List<TypeParameterNode> getTypeParameterNodes();

  interface TypeParameter {
    NameDeclarationNode getLocation();

    TypeParameterType getType();
  }
}
