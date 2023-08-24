/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.AsmStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.BlockDeclarationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodBodyNode;

public final class MethodBodyNodeImpl extends DelphiNodeImpl implements MethodBodyNode {
  private BlockDeclarationSectionNode declarationSection;
  private DelphiNode block;
  private CompoundStatementNode statementBlock;
  private AsmStatementNode asmBlock;

  public MethodBodyNodeImpl(Token token) {
    super(token);
  }

  public MethodBodyNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public boolean hasDeclarationSection() {
    return getChild(0) instanceof BlockDeclarationSectionNode;
  }

  @Override
  public BlockDeclarationSectionNode getDeclarationSection() {
    if (declarationSection == null && hasDeclarationSection()) {
      declarationSection = (BlockDeclarationSectionNode) getChild(0);
    }
    return declarationSection;
  }

  @Override
  public boolean hasStatementBlock() {
    return getBlock() instanceof CompoundStatementNode;
  }

  @Override
  public boolean hasAsmBlock() {
    return getBlock() instanceof AsmStatementNode;
  }

  @Override
  public CompoundStatementNode getStatementBlock() {
    if (statementBlock == null && hasStatementBlock()) {
      statementBlock = (CompoundStatementNode) getBlock();
    }
    return statementBlock;
  }

  @Override
  public AsmStatementNode getAsmBlock() {
    if (asmBlock == null && hasAsmBlock()) {
      asmBlock = (AsmStatementNode) getBlock();
    }
    return asmBlock;
  }

  @Override
  public DelphiNode getBlock() {
    if (block == null) {
      block = getChild(hasDeclarationSection() ? 1 : 0);
    }
    return block;
  }
}
