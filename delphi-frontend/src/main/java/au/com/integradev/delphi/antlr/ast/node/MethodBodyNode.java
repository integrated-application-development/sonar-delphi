package au.com.integradev.delphi.antlr.ast.node;

public interface MethodBodyNode extends DelphiNode {
  boolean hasDeclarationSection();

  BlockDeclarationSectionNode getDeclarationSection();

  boolean hasStatementBlock();

  boolean hasAsmBlock();

  CompoundStatementNode getStatementBlock();

  AsmStatementNode getAsmBlock();

  DelphiNode getBlock();
}
