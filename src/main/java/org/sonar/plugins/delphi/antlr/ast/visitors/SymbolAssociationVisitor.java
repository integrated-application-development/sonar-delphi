package org.sonar.plugins.delphi.antlr.ast.visitors;

import static com.google.common.base.Preconditions.checkNotNull;

import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolAssociationVisitor.Data;
import org.sonar.plugins.delphi.symbol.SymbolTable;
import org.sonar.plugins.delphi.symbol.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.UnitScope;

/**
 * Visitor for symbol association.
 *
 * <p>When the SymbolTableVisitor does its first pass over the codebase, it throws away ASTs to
 * clear up memory. The only information it retains is the symbol table itself.
 *
 * <p>This is all well and good from a memory management perspective, but it means we throw away the
 * link between symbols and their relevant AST nodes. (Scope associations, NameReferenceNode links,
 * NameDeclarationNode links...)
 *
 * <p>This visitor re-attaches applicable symbol information to the nodes of a fresh AST.
 */
public class SymbolAssociationVisitor implements DelphiParserVisitor<Data> {
  private static final String UNIT_DOES_NOT_EXIST = "Expected unit '%s' to exist in global scope.";

  public static class Data {
    private final SymbolTable symbolTable;
    private UnitScope unitScope;

    public Data(SymbolTable symbolTable) {
      this.symbolTable = symbolTable;
    }
  }

  @Override
  public Data visit(DelphiAST node, Data data) {
    if (node.jjtGetNumChildren() == 0) {
      return data;
    }

    String filePath = node.getDelphiFile().getSourceCodeFile().getAbsolutePath();

    UnitNameDeclaration declaration = data.symbolTable.getUnitByPath(filePath);
    String unitName = node.getFileHeader().getName();

    checkNotNull(declaration, String.format(UNIT_DOES_NOT_EXIST, unitName));

    data.unitScope = declaration.getUnitScope();
    node.setScope(data.unitScope);

    return node.childrenAccept(this, data);
  }

  @Override
  public Data visit(DelphiNode node, Data data) {
    data.unitScope.attach(node);
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(NameDeclarationNode node, Data data) {
    data.unitScope.attach(node);
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(NameReferenceNode node, Data data) {
    data.unitScope.attach(node);
    return DelphiParserVisitor.super.visit(node, data);
  }
}
