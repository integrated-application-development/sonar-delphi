package org.sonar.plugins.communitydelphi.api.ast;

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
