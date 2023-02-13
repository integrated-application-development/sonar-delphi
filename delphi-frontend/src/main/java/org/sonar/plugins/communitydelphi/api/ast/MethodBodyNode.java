package org.sonar.plugins.communitydelphi.api.ast;

public interface MethodBodyNode extends DelphiNode {
  boolean hasDeclarationSection();

  BlockDeclarationSectionNode getDeclarationSection();

  boolean hasStatementBlock();

  boolean hasAsmBlock();

  CompoundStatementNode getStatementBlock();

  AsmStatementNode getAsmBlock();

  DelphiNode getBlock();
}
