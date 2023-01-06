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
package org.sonar.plugins.delphi.antlr.ast.visitors;

import static com.google.common.base.Preconditions.checkNotNull;

import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.ArrayAccessorNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNameNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolAssociationVisitor.Data;
import org.sonar.plugins.delphi.symbol.SymbolTable;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.FileScope;

/**
 * Visitor for symbol association.
 *
 * <p>When the SymbolTableBuilder does its first pass over the codebase, it throws away ASTs to
 * clear up memory. The only information it retains is the symbol table itself.
 *
 * <p>This is all well and good from a memory management perspective, but it means we throw away the
 * link between symbols and their relevant AST nodes. (Scope associations, NameReferenceNode links,
 * NameDeclarationNode links...)
 *
 * <p>This visitor re-attaches applicable symbol information to the nodes of a fresh AST.
 */
public class SymbolAssociationVisitor implements DelphiParserVisitor<Data> {
  public static class Data {
    private final SymbolTable symbolTable;
    private FileScope fileScope;

    public Data(SymbolTable symbolTable) {
      this.symbolTable = symbolTable;
    }
  }

  @Override
  public Data visit(DelphiAST node, Data data) {
    String filePath = node.getDelphiFile().getSourceCodeFile().getAbsolutePath();

    UnitNameDeclaration declaration = data.symbolTable.getUnitByPath(filePath);
    String unitName = node.getFileHeader().getName();

    checkNotNull(declaration, "Expected unit '%s' to exist in global scope.", unitName);

    data.fileScope = declaration.getFileScope();
    node.setScope(data.fileScope);

    return node.childrenAccept(this, data);
  }

  @Override
  public Data visit(DelphiNode node, Data data) {
    data.fileScope.attach(node);
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(NameDeclarationNode node, Data data) {
    data.fileScope.attach(node);
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(NameReferenceNode node, Data data) {
    data.fileScope.attach(node);
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(ArrayAccessorNode node, Data data) {
    data.fileScope.attach(node);
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(MethodNameNode node, Data data) {
    data.fileScope.attach(node);
    return DelphiParserVisitor.super.visit(node, data);
  }
}
