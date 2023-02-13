package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;

public interface TypeParameterNode extends DelphiNode {
  List<NameDeclarationNode> getTypeParameterNameNodes();

  List<TypeReferenceNode> getTypeConstraintNodes();
}
