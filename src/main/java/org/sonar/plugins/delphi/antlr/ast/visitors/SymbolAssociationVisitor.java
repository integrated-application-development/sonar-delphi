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
  private static final String UNIT_DOES_NOT_EXIST = "Expected unit '%s' to exist in global scope.";

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

    checkNotNull(declaration, String.format(UNIT_DOES_NOT_EXIST, unitName));

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
