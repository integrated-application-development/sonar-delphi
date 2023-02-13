package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.type.Typed;

public interface VarDeclarationNode extends DelphiNode, Typed {
  NameDeclarationListNode getNameDeclarationList();

  VarSectionNode getVarSection();

  TypeNode getTypeNode();

  boolean isAbsolute();
}
