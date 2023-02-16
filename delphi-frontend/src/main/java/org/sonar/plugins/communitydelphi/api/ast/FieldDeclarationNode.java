package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.type.Typed;

public interface FieldDeclarationNode extends DelphiNode, Typed, Visibility {
  @Override
  VisibilityType getVisibility();

  NameDeclarationListNode getDeclarationList();

  TypeNode getTypeNode();
}
