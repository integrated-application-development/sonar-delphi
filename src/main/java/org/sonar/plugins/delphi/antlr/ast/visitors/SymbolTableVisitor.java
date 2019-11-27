package org.sonar.plugins.delphi.antlr.ast.visitors;

import static org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.ResolutionLevel.COMPLETE;
import static org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.ResolutionLevel.INTERFACE;
import static org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.ResolutionLevel.NONE;
import static org.sonar.plugins.delphi.symbol.VariableNameDeclaration.compilerVariable;
import static org.sonar.plugins.delphi.symbol.resolve.NameResolver.resolve;

import com.google.common.base.Preconditions;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nullable;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.AnonymousMethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.CaseStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ConstDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.EnumElementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExceptItemNode;
import org.sonar.plugins.delphi.antlr.ast.node.FieldDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.FileHeaderNode;
import org.sonar.plugins.delphi.antlr.ast.node.FinalizationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ImplementationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InitializationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InterfaceSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNameNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ProcedureTypeHeadingNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyNode;
import org.sonar.plugins.delphi.antlr.ast.node.RepeatStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.TryStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarNameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.WithStatementNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.Data;
import org.sonar.plugins.delphi.symbol.DeclarationScope;
import org.sonar.plugins.delphi.symbol.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.DelphiScope;
import org.sonar.plugins.delphi.symbol.EnumElementNameDeclaration;
import org.sonar.plugins.delphi.symbol.ImportResolutionHandler;
import org.sonar.plugins.delphi.symbol.LocalScope;
import org.sonar.plugins.delphi.symbol.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.MethodScope;
import org.sonar.plugins.delphi.symbol.PropertyNameDeclaration;
import org.sonar.plugins.delphi.symbol.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.TypeScope;
import org.sonar.plugins.delphi.symbol.UnitImportNameDeclaration;
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
 * <p>Each scope object is linked to its parent scope, which is the scope object of the next
 * embedding syntactic entity that has a scope.
 *
 * <p>This visitor also finds occurrences of the declarations and creates NameOccurrence objects
 * accordingly
 */
public class SymbolTableVisitor implements DelphiParserVisitor<Data> {
  public enum ResolutionLevel {
    NONE,
    INTERFACE,
    COMPLETE
  }

  public static class Data {
    private final ResolutionLevel resolved;
    private final ResolutionLevel resolutionLevel;
    private final ImportResolutionHandler importHandler;
    private final Deque<DelphiScope> scopes;
    private UnitNameDeclaration unitDeclaration;

    public Data(
        ResolutionLevel resolved,
        ResolutionLevel resolutionLevel,
        ImportResolutionHandler importHandler,
        @Nullable UnitNameDeclaration unitDeclaration) {
      this.resolved = resolved;
      this.resolutionLevel = resolutionLevel;
      this.importHandler = importHandler;
      this.scopes = new ArrayDeque<>();
      this.unitDeclaration = unitDeclaration;
      if (unitDeclaration != null) {
        scopes.add(unitDeclaration.getUnitScope());
      }
    }

    @Nullable
    public UnitNameDeclaration getUnitDeclaration() {
      return unitDeclaration;
    }

    private DelphiScope currentScope() {
      Preconditions.checkState(!scopes.isEmpty());
      return scopes.peek();
    }

    /**
     * Sets the scope of a node and adjusts the scope stack accordingly. The scope on top of the
     * stack is set as the parent of the given scope, which is then also stored on the scope stack.
     *
     * @param newScope the scope to be added to the stack
     * @param node the AST node which the scope is associated to
     */
    private void addScope(DelphiScope newScope, DelphiNode node) {
      unitDeclaration.getUnitScope().registerScope(node, newScope);
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
      newScope.setParent(currentScope());
      scopes.push(newScope);
    }

    private void addDeclaration(DelphiNameDeclaration declaration, NameDeclarationNode nameNode) {
      unitDeclaration.getUnitScope().registerDeclaration(nameNode, declaration);
      nameNode.setNameDeclaration(declaration);
      addDeclaration(declaration);
    }

    private void addDeclaration(DelphiNameDeclaration declaration) {
      currentScope().addDeclaration(declaration);
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
      data.addDeclaration(compilerVariable("Result", node.getReturnType(), methodScope));
    }

    TypeNameDeclaration methodType = node.getTypeDeclaration();
    if (methodType != null) {
      data.addDeclaration(compilerVariable("Self", methodType.getType(), methodScope));
    }

    return visitScope(node, data);
  }

  private Data createTypeScope(TypeDeclarationNode node, Data data) {
    resolve(node);

    TypeScope typeScope = new TypeScope();
    node.getTypeNode().setScope(typeScope);

    TypeNameDeclaration declaration = new TypeNameDeclaration(node);
    typeScope.setTypeDeclaration(declaration);
    data.addDeclaration(declaration, node.getTypeNameNode());

    data.addScope(typeScope, node.getTypeNode());
    visitScope(node, data);

    Type superType = node.getTypeNode().getType().superType();
    typeScope.setParent(
        (superType instanceof ScopedType) ? ((ScopedType) superType).typeScope() : null);

    return data;
  }

  private Data createUnitScope(DelphiAST node, Data data) {
    FileHeaderNode fileHeader = node.getFileHeader();
    UnitScope unitScope = new UnitScope(fileHeader.getName());
    data.scopes.add(unitScope);
    node.setScope(unitScope);

    data.unitDeclaration = new UnitNameDeclaration(fileHeader, unitScope);
    NameDeclarationNode declarationNode = fileHeader.getNameNode();
    data.addDeclaration(data.unitDeclaration, declarationNode);

    return visitScope(node, data);
  }

  private Data visitScope(DelphiNode node, Data data) {
    DelphiParserVisitor.super.visit(node, data);
    data.scopes.pop();
    return data;
  }

  @Override
  public Data visit(DelphiAST node, Data data) {
    if (node.jjtGetNumChildren() == 0
        || data.resolved == COMPLETE
        || data.resolutionLevel == NONE) {
      return data;
    }

    FileHeaderNode header = node.getFileHeader();
    if (!(header instanceof UnitDeclarationNode) && data.resolutionLevel != COMPLETE) {
      // Unit files use a 2-pass symbol resolution approach to conserve memory
      // Program, Package, and Library files don't need this.
      return data;
    }

    if (!data.scopes.isEmpty()) {
      node.setScope(data.currentScope());
      return visitScope(node, data);
    }

    return createUnitScope(node, data);
  }

  @Override
  public Data visit(InterfaceSectionNode node, Data data) {
    if (data.resolved == INTERFACE) {
      return data;
    }
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(ImplementationSectionNode node, Data data) {
    if (data.resolutionLevel != COMPLETE) {
      return data;
    }
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(InitializationSectionNode node, Data data) {
    if (data.resolutionLevel != COMPLETE) {
      return data;
    }
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(FinalizationSectionNode node, Data data) {
    if (data.resolutionLevel != COMPLETE) {
      return data;
    }
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(UnitImportNode node, Data data) {
    UnitImportNameDeclaration importDeclaration = data.importHandler.apply(node);
    data.addDeclaration(importDeclaration, node.getNameNode());
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(TypeDeclarationNode node, Data data) {
    return createTypeScope(node, data);
  }

  @Override
  public Data visit(EnumElementNode node, Data data) {
    EnumElementNameDeclaration declaration = new EnumElementNameDeclaration(node);
    data.addDeclaration(declaration, node.getNameDeclarationNode());
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
    data.addDeclaration(declaration, node.getMethodName());

    return createDeclarationScope(node, data);
  }

  @Override
  public Data visit(PropertyNode node, Data data) {
    resolve(node);

    PropertyNameDeclaration declaration = new PropertyNameDeclaration(node);
    data.addDeclaration(declaration, node.getPropertyName());

    return createDeclarationScope(node, data);
  }

  @Override
  public Data visit(ProcedureTypeHeadingNode node, Data data) {
    createDeclarationScope(node, data);
    return data;
  }

  @Override
  public Data visit(MethodImplementationNode node, Data data) {
    boolean isImplementationMethod = !resolve(node) && node.getMethodName().nextName() == null;

    if (isImplementationMethod) {
      MethodNameDeclaration declaration = new MethodNameDeclaration(node);
      MethodNameNode nameNode = node.getMethodHeading().getMethodNameNode();
      data.addDeclaration(declaration, nameNode);
    }

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
    data.addDeclaration(declaration, node);
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
