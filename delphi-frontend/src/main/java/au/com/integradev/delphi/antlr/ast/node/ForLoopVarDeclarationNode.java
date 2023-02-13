package au.com.integradev.delphi.antlr.ast.node;

import javax.annotation.Nullable;

public interface ForLoopVarDeclarationNode extends ForLoopVarNode {
  NameDeclarationNode getNameDeclarationNode();

  @Nullable
  TypeNode getTypeNode();
}
