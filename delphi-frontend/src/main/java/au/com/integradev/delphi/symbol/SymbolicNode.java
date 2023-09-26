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
package au.com.integradev.delphi.symbol;

import au.com.integradev.delphi.symbol.declaration.UnitNameDeclarationImpl;
import java.util.concurrent.atomic.AtomicInteger;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

public final class SymbolicNode implements Node {
  private static final AtomicInteger IMAGINARY_TOKEN_INDEX = new AtomicInteger(Integer.MIN_VALUE);
  private final DelphiTokenType tokenType;
  private final int tokenIndex;
  private final String image;
  private final int beginLine;
  private final int endLine;
  private final int beginColumn;
  private final int endColumn;
  private final DelphiScope scope;

  public SymbolicNode(DelphiNode node) {
    this(node, node.getScope());
  }

  public SymbolicNode(DelphiNode node, DelphiScope scope) {
    this(
        node.getTokenType(),
        node.getTokenIndex(),
        node.getImage(),
        node.getBeginLine(),
        node.getEndLine(),
        node.getBeginColumn(),
        node.getEndColumn(),
        scope);
  }

  private SymbolicNode(
      DelphiTokenType tokenType,
      int tokenIndex,
      String image,
      int beginLine,
      int endLine,
      int beginColumn,
      int endColumn,
      DelphiScope scope) {
    this.tokenType = tokenType;
    this.tokenIndex = tokenIndex;
    this.image = image;
    this.beginLine = beginLine;
    this.endLine = endLine;
    this.beginColumn = beginColumn;
    this.endColumn = endColumn;
    this.scope = scope;
  }

  public static SymbolicNode imaginary(String image, DelphiScope scope) {
    return new SymbolicNode(
        DelphiTokenType.INVALID, IMAGINARY_TOKEN_INDEX.incrementAndGet(), image, 0, 0, 0, 0, scope);
  }

  public static SymbolicNode fromRange(String image, DelphiNode begin, DelphiNode end) {
    return new SymbolicNode(
        begin.getTokenType(),
        begin.getTokenIndex(),
        image,
        begin.getBeginLine(),
        end.getEndLine(),
        begin.getBeginColumn(),
        end.getEndColumn(),
        begin.getScope());
  }

  @Override
  public DelphiTokenType getTokenType() {
    return tokenType;
  }

  @Override
  public int getTokenIndex() {
    return tokenIndex;
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public int getBeginLine() {
    return beginLine;
  }

  @Override
  public int getBeginColumn() {
    return beginColumn;
  }

  @Override
  public int getEndLine() {
    return endLine;
  }

  @Override
  public int getEndColumn() {
    return endColumn;
  }

  @Override
  public DelphiScope getScope() {
    return scope;
  }

  @Override
  public String getUnitName() {
    FileScope fileScope = scope.getEnclosingScope(FileScope.class);
    if (fileScope == null) {
      return UnitNameDeclarationImpl.UNKNOWN_UNIT;
    }
    return fileScope.getUnitDeclaration().fullyQualifiedName();
  }
}
