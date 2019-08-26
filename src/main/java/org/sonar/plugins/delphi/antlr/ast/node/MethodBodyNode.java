package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class MethodBodyNode extends DelphiNode {
  private BlockDeclarationSectionNode declarationSection;
  private DelphiNode block;
  private CompoundStatementNode statementBlock;
  private AsmStatementNode asmBlock;

  public MethodBodyNode(Token token) {
    super(token);
  }

  public MethodBodyNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public boolean hasDeclarationSection() {
    return jjtGetChild(0) instanceof BlockDeclarationSectionNode;
  }

  public BlockDeclarationSectionNode getDeclarationSection() {
    if (declarationSection == null && hasDeclarationSection()) {
      declarationSection = (BlockDeclarationSectionNode) jjtGetChild(0);
    }
    return declarationSection;
  }

  public boolean hasStatementBlock() {
    return getBlock() instanceof CompoundStatementNode;
  }

  public boolean hasAsmBlock() {
    return getBlock() instanceof AsmStatementNode;
  }

  public CompoundStatementNode getStatementBlock() {
    if (statementBlock == null && hasStatementBlock()) {
      statementBlock = (CompoundStatementNode) getBlock();
    }
    return statementBlock;
  }

  public AsmStatementNode getAsmBlock() {
    if (asmBlock == null && hasAsmBlock()) {
      asmBlock = (AsmStatementNode) getBlock();
    }
    return asmBlock;
  }

  public DelphiNode getBlock() {
    if (block == null) {
      block = (DelphiNode) jjtGetChild(hasDeclarationSection() ? 1 : 0);
    }
    return block;
  }
}
