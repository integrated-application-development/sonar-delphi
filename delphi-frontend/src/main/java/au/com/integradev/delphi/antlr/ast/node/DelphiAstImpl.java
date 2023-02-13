/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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

import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.ast.token.DelphiToken;
import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import au.com.integradev.delphi.file.DelphiFile;
import au.com.integradev.delphi.pmd.FilePosition;
import java.util.List;
import java.util.Set;

/** DelphiLanguage AST tree. */
public class DelphiAstImpl extends AbstractDelphiNode implements DelphiAst {
  private final DelphiFile delphiFile;

  /**
   * Constructor.
   *
   * @param delphiFile The DelphiFile that this AST represents
   * @param root The root node of the AST
   */
  public DelphiAstImpl(DelphiFile delphiFile, DelphiNode root) {
    super(DelphiLexer.TkRootNode);
    this.delphiFile = delphiFile;

    if (root != null) {
      for (int i = 0; i < root.jjtGetNumChildren(); ++i) {
        jjtAddChild(root.jjtGetChild(i));
      }
    }
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public int getBeginLine() {
    return FilePosition.UNDEFINED_LINE;
  }

  @Override
  public int getBeginColumn() {
    return FilePosition.UNDEFINED_COLUMN;
  }

  @Override
  public int getEndLine() {
    return FilePosition.UNDEFINED_LINE;
  }

  @Override
  public int getEndColumn() {
    return FilePosition.UNDEFINED_COLUMN;
  }

  @Override
  public List<DelphiToken> getComments() {
    return delphiFile.getComments();
  }

  @Override
  public List<DelphiToken> getCommentsInsideNode(DelphiNode node) {
    return getCommentsBetweenTokens(node.jjtGetFirstToken(), node.jjtGetLastToken());
  }

  @Override
  public List<DelphiToken> getTokens() {
    return delphiFile.getTokens();
  }

  @Override
  public DelphiFile getDelphiFile() {
    return delphiFile;
  }

  @Override
  public String getFileName() {
    return delphiFile.getSourceCodeFile().getAbsolutePath();
  }

  @Override
  public Set<Integer> getSuppressions() {
    return delphiFile.getSuppressions();
  }

  @Override
  public FileHeaderNode getFileHeader() {
    return (FileHeaderNode) jjtGetChild(0);
  }

  @Override
  public boolean isProgram() {
    return jjtGetNumChildren() > 0 && getFileHeader() instanceof ProgramDeclarationNode;
  }

  @Override
  public boolean isUnit() {
    return jjtGetNumChildren() > 0 && getFileHeader() instanceof UnitDeclarationNode;
  }

  @Override
  public boolean isPackage() {
    return jjtGetNumChildren() > 0 && getFileHeader() instanceof PackageDeclarationNode;
  }
}
