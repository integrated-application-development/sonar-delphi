package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.type.Typed;

public interface VarDeclarationNode extends DelphiNode, Typed {
  NameDeclarationListNode getNameDeclarationList();

  VarSectionNode getVarSection();

  TypeNode getTypeNode();

  boolean isAbsolute();
}
