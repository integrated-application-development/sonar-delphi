package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.type.Typed;

public interface FieldDeclarationNode extends DelphiNode, Typed, Visibility {
  @Override
  VisibilityType getVisibility();

  NameDeclarationListNode getDeclarationList();

  TypeNode getTypeNode();
}
