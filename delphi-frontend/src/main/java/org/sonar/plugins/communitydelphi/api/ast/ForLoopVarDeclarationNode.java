package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nullable;

public interface ForLoopVarDeclarationNode extends ForLoopVarNode {
  NameDeclarationNode getNameDeclarationNode();

  @Nullable
  TypeNode getTypeNode();
}
