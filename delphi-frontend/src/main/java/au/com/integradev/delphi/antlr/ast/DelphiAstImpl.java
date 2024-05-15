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
package au.com.integradev.delphi.antlr.ast;

import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.ast.node.DelphiNodeImpl;
import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import au.com.integradev.delphi.file.DelphiFile;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FileHeaderNode;
import org.sonar.plugins.communitydelphi.api.ast.PackageDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ProgramDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.UnitDeclarationNode;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

/** DelphiLanguage AST tree. */
public class DelphiAstImpl extends DelphiNodeImpl implements DelphiAst {
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
      root.getChildren().forEach(this::addChild);
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
  public DelphiFile getDelphiFile() {
    return delphiFile;
  }

  @Nullable
  @Override
  public FileHeaderNode getFileHeader() {
    DelphiNode fileHeader = getChild(0);
    return fileHeader instanceof FileHeaderNode ? (FileHeaderNode) fileHeader : null;
  }

  @Override
  public boolean isProgram() {
    FileHeaderNode fileHeader = getFileHeader();
    return fileHeader instanceof ProgramDeclarationNode || fileHeader == null;
  }

  @Override
  public boolean isUnit() {
    return getFileHeader() instanceof UnitDeclarationNode;
  }

  @Override
  public boolean isPackage() {
    return getFileHeader() instanceof PackageDeclarationNode;
  }

  public List<DelphiToken> getCommentsInsideNode(DelphiNode node) {
    return getCommentsBetweenTokens(node.getFirstToken(), node.getLastToken());
  }

  private List<DelphiToken> getCommentsBetweenTokens(DelphiToken first, DelphiToken last) {
    return getComments().stream()
        .filter(
            token -> {
              int index = token.getIndex();
              return index > first.getIndex() && index < last.getIndex();
            })
        .collect(Collectors.toList());
  }
}
