package org.sonar.plugins.delphi.antlr.ast.visitors;

import static org.sonar.plugins.delphi.symbol.resolve.NameResolver.resolve;

import com.google.common.base.Preconditions;
import java.util.ArrayDeque;
import java.util.Deque;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.AnonymousMethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.CaseStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ConstDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.EnumElementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExceptItemNode;
import org.sonar.plugins.delphi.antlr.ast.node.FieldDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ProcedureTypeHeadingNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyNode;
import org.sonar.plugins.delphi.antlr.ast.node.RepeatStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.TryStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarNameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.WithStatementNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.Data;
import org.sonar.plugins.delphi.symbol.DeclarationScope;
import org.sonar.plugins.delphi.symbol.DelphiScope;
import org.sonar.plugins.delphi.symbol.EnumElementNameDeclaration;
import org.sonar.plugins.delphi.symbol.GlobalScope;
import org.sonar.plugins.delphi.symbol.LocalScope;
import org.sonar.plugins.delphi.symbol.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.MethodScope;
import org.sonar.plugins.delphi.symbol.PropertyNameDeclaration;
import org.sonar.plugins.delphi.symbol.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.TypeScope;
import org.sonar.plugins.delphi.symbol.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.UnitScope;
import org.sonar.plugins.delphi.symbol.VariableNameDeclaration;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ScopedType;

/**
 * Visitor for symbol table creation.
 *
 * <p>Visits all nodes of an AST and creates scope objects for nodes representing syntactic entities
 * which may contain declarations. For example, a block may contain variable definitions (which are
 * declarations) and therefore needs a scope object where these declarations can be associated,
 * whereas an expression can't contain declarations and therefore doesn't need a scope.
 *
 * <p>With the exception of global scopes, each scope object is linked to its parent scope, which is
 * the scope object of the next embedding syntactic entity that has a scope.
 *
 * <p>This visitor also finds occurrences of the declarations and creates NameOccurrence objects
 * accordingly
 */
public class SymbolTableVisitor implements DelphiParserVisitor<Data> {

  public static class Data {
    private final Deque<DelphiScope> scopes = new ArrayDeque<>();

    /**
     * Adds a scope to the scope stack and assigns the scope to a given AST node
     *
     * @param newScope the scope for the node.
     * @param node the AST node for which the scope is to be set.
     */
    private void addScope(DelphiScope newScope, DelphiNode node) {
      addScope(newScope);
      node.setScope(newScope);
    }

    /**
     * Sets the scope of a node and adjusts the scope stack accordingly. The scope on top of the
     * stack is set as the parent of the given scope, which is then also stored on the scope stack.
     *
     * @param newScope the scope to be added to the stack
     */
    private void addScope(DelphiScope newScope) {
      newScope.setParent(scopes.peek());
      scopes.push(newScope);
    }

    private void addDeclaration(NameDeclaration declaration) {
      Preconditions.checkState(!scopes.isEmpty());
      scopes.peek().addDeclaration(declaration);
    }
  }

  private Data createDeclarationScope(DelphiNode node, Data data) {
    data.addScope(new DeclarationScope());
    return visitScope(node, data);
  }

  private Data createLocalScope(DelphiNode node, Data data) {
    data.addScope(new LocalScope(), node);
    return visitScope(node, data);
  }

  private Data createMethodScope(MethodImplementationNode node, Data data) {
    MethodScope methodScope = new MethodScope(node);
    data.addScope(methodScope, node);

    if (node.isFunction() || node.isOperator()) {
      data.addDeclaration(
          VariableNameDeclaration.compilerVariable("Result", node.getReturnType(), methodScope));
    }

    return visitScope(node, data);
  }

  private Data createTypeScope(TypeDeclarationNode node, Data data) {
    resolve(node);

    TypeNameDeclaration declaration = new TypeNameDeclaration(node);
    node.getTypeNameNode().setNameDeclaration(declaration);

    data.addDeclaration(declaration);
    DelphiScope typeScope = new TypeScope(declaration);
    data.addScope(typeScope, node.getTypeNode());
    visitScope(node, data);

    Type superType = node.getTypeNode().getType().superType();
    typeScope.setParent(
        (superType instanceof ScopedType) ? ((ScopedType) superType).typeScope() : null);

    return data;
  }

  private Data createUnitScope(DelphiAST node, Data data) {
    // When we do full symbol resolution, a single GlobalScope will be persisted across all files.
    GlobalScope globalScope = new GlobalScope();
    data.scopes.add(globalScope);

    UnitScope unitScope = new UnitScope();
    var declaration = new UnitNameDeclaration(node.getFileHeader(), globalScope, unitScope);
    node.getFileHeader().getNameDeclaration().setNameDeclaration(declaration);
    data.addDeclaration(declaration);
    data.addScope(unitScope, node);

    return visitScope(node, data);
  }

  private Data visitScope(DelphiNode node, Data data) {
    DelphiParserVisitor.super.visit(node, data);
    data.scopes.pop();
    return data;
  }

  @Override
  public Data visit(DelphiAST node, Data data) {
    return createUnitScope(node, data);
  }

  @Override
  public Data visit(UnitImportNode node, Data data) {
    UnitNameDeclaration declaration = new UnitNameDeclaration(node);
    node.getNameDeclaration().setNameDeclaration(declaration);
    data.addDeclaration(declaration);
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(TypeDeclarationNode node, Data data) {
    return createTypeScope(node, data);
  }

  @Override
  public Data visit(EnumElementNode node, Data data) {
    EnumElementNameDeclaration declaration = new EnumElementNameDeclaration(node);
    node.getNameDeclarationNode().setNameDeclaration(declaration);
    data.addDeclaration(declaration);
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(CompoundStatementNode node, Data data) {
    // top-level blocks for methods should have the same scope as parameters, just skip them
    // same applies to catch statements defining exceptions + the catch block, and for-blocks
    if (node.jjtGetParent() instanceof MethodBodyNode
        || node.jjtGetParent() instanceof ExceptItemNode
        || node.jjtGetParent() instanceof ForStatementNode) {
      return DelphiParserVisitor.super.visit(node, data);
    }
    return createLocalScope(node, data);
  }

  @Override
  public Data visit(ExceptItemNode node, Data data) {
    return createLocalScope(node, data);
  }

  @Override
  public Data visit(MethodDeclarationNode node, Data data) {
    resolve(node);

    MethodNameDeclaration declaration = new MethodNameDeclaration(node);
    node.getMethodName().setNameDeclaration(declaration);
    data.addDeclaration(declaration);

    return createDeclarationScope(node, data);
  }

  @Override
  public Data visit(PropertyNode node, Data data) {
    resolve(node);

    PropertyNameDeclaration declaration = new PropertyNameDeclaration(node);
    node.getPropertyName().setNameDeclaration(declaration);
    data.addDeclaration(declaration);

    return createDeclarationScope(node, data);
  }

  @Override
  public Data visit(ProcedureTypeHeadingNode node, Data data) {
    createDeclarationScope(node, data);
    return data;
  }

  @Override
  public Data visit(MethodImplementationNode node, Data data) {
    resolve(node);
    return createMethodScope(node, data);
  }

  @Override
  public Data visit(AnonymousMethodNode node, Data data) {
    data.addScope(new LocalScope(), node);
    resolve(node.getMethodParametersNode());
    resolve(node.getReturnTypeNode());
    return visitScope(node, data);
  }

  @Override
  public Data visit(TryStatementNode node, Data data) {
    return createLocalScope(node, data);
  }

  @Override
  public Data visit(ForStatementNode node, Data data) {
    return createLocalScope(node, data);
  }

  @Override
  public Data visit(RepeatStatementNode node, Data data) {
    return createLocalScope(node, data);
  }

  @Override
  public Data visit(WithStatementNode node, Data data) {
    return createLocalScope(node, data);
  }

  @Override
  public Data visit(CaseStatementNode node, Data data) {
    return createLocalScope(node, data);
  }

  @Override
  public Data visit(VarDeclarationNode node, Data data) {
    resolve(node.getTypeNode());
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(FieldDeclarationNode node, Data data) {
    resolve(node.getTypeNode());
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(ConstDeclarationNode node, Data data) {
    TypeNode typeNode = node.getTypeNode();
    if (typeNode != null) {
      resolve(node.getTypeNode());
    }
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(VarNameDeclarationNode node, Data data) {
    VariableNameDeclaration declaration = new VariableNameDeclaration(node);
    data.addDeclaration(declaration);
    node.setNameDeclaration(declaration);
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(PrimaryExpressionNode node, Data data) {
    node.findDescendantsOfType(AnonymousMethodNode.class)
        .forEach(method -> this.visit(method, data));
    resolve(node);
    return data;
  }
}
