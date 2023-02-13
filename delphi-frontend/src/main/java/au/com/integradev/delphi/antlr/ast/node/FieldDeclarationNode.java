package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.type.Typed;

public interface FieldDeclarationNode extends DelphiNode, Typed, Visibility {
  @Override
  VisibilityType getVisibility();

  NameDeclarationListNode getDeclarationList();

  TypeNode getTypeNode();
}
