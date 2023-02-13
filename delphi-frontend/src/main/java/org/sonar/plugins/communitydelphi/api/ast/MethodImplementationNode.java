package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface MethodImplementationNode extends MethodNode, Visibility {
  MethodBodyNode getMethodBody();

  @Nullable
  BlockDeclarationSectionNode getDeclarationSection();

  @Nullable
  DelphiNode getBlock();

  @Nullable
  CompoundStatementNode getStatementBlock();

  @Nullable
  AsmStatementNode getAsmBlock();

  boolean isEmptyMethod();

  @Nonnull
  NameReferenceNode getNameReferenceNode();
}
