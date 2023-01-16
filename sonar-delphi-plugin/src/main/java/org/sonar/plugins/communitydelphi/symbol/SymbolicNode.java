/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package org.sonar.plugins.communitydelphi.symbol;

import java.util.concurrent.atomic.AtomicInteger;
import net.sourceforge.pmd.lang.ast.AbstractNode;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.ScopedNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.IndexedNode;
import org.sonar.plugins.communitydelphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.communitydelphi.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.symbol.scope.FileScope;

public final class SymbolicNode extends AbstractNode implements ScopedNode, IndexedNode {
  private static final AtomicInteger IMAGINARY_TOKEN_INDEX = new AtomicInteger(Integer.MIN_VALUE);
  private final DelphiScope scope;
  private final int tokenIndex;

  public SymbolicNode(DelphiNode node) {
    this(node, node.getScope());
  }

  public SymbolicNode(DelphiNode node, DelphiScope scope) {
    this(
        node.jjtGetId(),
        node.getBeginLine(),
        node.getEndLine(),
        node.getBeginColumn(),
        node.getEndColumn(),
        node.getImage(),
        scope,
        node.getTokenIndex());
  }

  private SymbolicNode(
      int id,
      int beginLine,
      int endLine,
      int beginColumn,
      int endColumn,
      String image,
      DelphiScope scope,
      int tokenIndex) {
    super(id, beginLine, endLine, beginColumn, endColumn);
    this.setImage(image);
    this.scope = scope;
    this.tokenIndex = tokenIndex;
  }

  public static SymbolicNode imaginary(String image, DelphiScope scope) {
    return new SymbolicNode(0, 0, 0, 0, 0, image, scope, IMAGINARY_TOKEN_INDEX.incrementAndGet());
  }

  public static SymbolicNode fromRange(String image, DelphiNode begin, Node end) {
    return new SymbolicNode(
        begin.jjtGetId(),
        begin.getBeginLine(),
        end.getEndLine(),
        begin.getBeginColumn(),
        end.getEndColumn(),
        image,
        begin.getScope(),
        begin.getTokenIndex());
  }

  @Override
  public DelphiScope getScope() {
    return scope;
  }

  @Override
  public int getTokenIndex() {
    return tokenIndex;
  }

  public String getUnitName() {
    FileScope fileScope = scope.getEnclosingScope(FileScope.class);
    if (fileScope == null) {
      return UnitNameDeclaration.UNKNOWN_UNIT;
    }
    return fileScope.getUnitDeclaration().fullyQualifiedName();
  }

  @Override
  public String getXPathNodeName() {
    return getImage();
  }
}
