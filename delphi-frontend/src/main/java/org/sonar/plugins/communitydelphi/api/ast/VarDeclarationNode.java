package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.type.Typed;

public interface VarDeclarationNode extends DelphiNode, Typed {
  NameDeclarationListNode getNameDeclarationList();

  VarSectionNode getVarSection();

  TypeNode getTypeNode();

  boolean isAbsolute();
}
