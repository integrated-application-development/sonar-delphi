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
package au.com.integradev.delphi.antlr.ast.visitors;

import static au.com.integradev.delphi.symbol.declaration.VariableNameDeclarationImpl.compilerVariable;

import au.com.integradev.delphi.antlr.ast.DelphiAstImpl;
import au.com.integradev.delphi.antlr.ast.node.MutableDelphiNode;
import au.com.integradev.delphi.antlr.ast.node.NameDeclarationNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.WithStatementNodeImpl;
import au.com.integradev.delphi.antlr.ast.visitors.SymbolTableVisitor.Data;
import au.com.integradev.delphi.preprocessor.CompilerSwitchRegistry;
import au.com.integradev.delphi.symbol.ImportResolutionHandler;
import au.com.integradev.delphi.symbol.NameOccurrenceImpl;
import au.com.integradev.delphi.symbol.SymbolicNode;
import au.com.integradev.delphi.symbol.declaration.EnumElementNameDeclarationImpl;
import au.com.integradev.delphi.symbol.declaration.MethodNameDeclarationImpl;
import au.com.integradev.delphi.symbol.declaration.PropertyNameDeclarationImpl;
import au.com.integradev.delphi.symbol.declaration.TypeNameDeclarationImpl;
import au.com.integradev.delphi.symbol.declaration.TypeParameterNameDeclarationImpl;
import au.com.integradev.delphi.symbol.declaration.UnitNameDeclarationImpl;
import au.com.integradev.delphi.symbol.declaration.VariableNameDeclarationImpl;
import au.com.integradev.delphi.symbol.resolve.NameResolutionHelper;
import au.com.integradev.delphi.symbol.scope.DeclarationScopeImpl;
import au.com.integradev.delphi.symbol.scope.DelphiScopeImpl;
import au.com.integradev.delphi.symbol.scope.LocalScopeImpl;
import au.com.integradev.delphi.symbol.scope.MethodScopeImpl;
import au.com.integradev.delphi.symbol.scope.SysInitScopeImpl;
import au.com.integradev.delphi.symbol.scope.SystemScopeImpl;
import au.com.integradev.delphi.symbol.scope.TypeScopeImpl;
import au.com.integradev.delphi.symbol.scope.UnitScopeImpl;
import au.com.integradev.delphi.symbol.scope.WithScopeImpl;
import au.com.integradev.delphi.type.factory.ClassReferenceTypeImpl;
import au.com.integradev.delphi.type.factory.PointerTypeImpl;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayConstructorNode;
import org.sonar.plugins.communitydelphi.api.ast.CaseStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ClassReferenceTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ConstStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.EnumElementNode;
import org.sonar.plugins.communitydelphi.api.ast.EnumTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptItemNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.FieldDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.FileHeaderNode;
import org.sonar.plugins.communitydelphi.api.ast.FinalizationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.ForInStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.ForStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.ForToStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.ast.GenericDefinitionNode;
import org.sonar.plugins.communitydelphi.api.ast.GenericDefinitionNode.TypeParameter;
import org.sonar.plugins.communitydelphi.api.ast.ImplementationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.InitializationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.InterfaceSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodBodyNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodNameNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodParametersNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodResolutionClauseNode;
import org.sonar.plugins.communitydelphi.api.ast.NameDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PointerTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ProcedureTypeHeadingNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordVariantTagNode;
import org.sonar.plugins.communitydelphi.api.ast.RepeatStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.TryStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeParameterNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.UnitDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.UnitImportNode;
import org.sonar.plugins.communitydelphi.api.ast.VarDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.VarStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.WithStatementNode;
import org.sonar.plugins.communitydelphi.api.directive.SwitchDirective.SwitchKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.EnumElementNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DeclarationScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.MethodScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.SysInitScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.SystemScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.TypeScope;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.HelperType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;
import org.sonar.plugins.communitydelphi.api.type.TypeUtils;

/**
 * Visitor for symbol table creation.
 *
 * <p>Visits nodes of an AST and creates scope objects for nodes representing syntactic entities
 * which may contain declarations. For example, a block may contain variable definitions (which are
 * declarations) and therefore needs a scope object where these declarations can be associated,
 * whereas an expression can't contain declarations and therefore doesn't need a scope.
 *
 * <p>Each scope object is linked to its parent scope, which is the scope object of the next
 * embedding syntactic entity that has a scope.
 *
 * <p>Also finds occurrences of the declarations and creates NameOccurrence objects accordingly.
 */
public abstract class SymbolTableVisitor implements DelphiParserVisitor<Data> {
  public static class Data {
    protected final TypeFactory typeFactory;
    protected final NameResolutionHelper nameResolutionHelper;
    protected final CompilerSwitchRegistry switchRegistry;
    protected final ImportResolutionHandler importHandler;
    protected final SystemScope systemScope;
    protected final SysInitScope sysInitScope;
    protected final Deque<DelphiScope> scopes;
    protected UnitNameDeclaration unitDeclaration;

    public Data(
        TypeFactory typeFactory,
        CompilerSwitchRegistry switchRegistry,
        ImportResolutionHandler importHandler,
        @Nullable SystemScope systemScope,
        @Nullable SysInitScope sysInitScope,
        @Nullable UnitNameDeclaration unitDeclaration) {
      this.typeFactory = typeFactory;
      this.nameResolutionHelper = new NameResolutionHelper(typeFactory);
      this.switchRegistry = switchRegistry;
      this.importHandler = importHandler;
      this.systemScope = systemScope;
      this.sysInitScope = sysInitScope;
      this.scopes = new ArrayDeque<>();
      this.unitDeclaration = unitDeclaration;
      if (unitDeclaration != null) {
        scopes.add(unitDeclaration.getFileScope());
      }
    }

    @Nullable
    public UnitNameDeclaration getUnitDeclaration() {
      return unitDeclaration;
    }

    protected DelphiScope currentScope() {
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
    protected void addScope(DelphiScope newScope, DelphiNode node) {
      unitDeclaration.getFileScope().registerScope(node, newScope);
      addScope(newScope);
      ((MutableDelphiNode) node).setScope(newScope);
    }

    /**
     * Add a new scope to the stack
     *
     * @param newScope the scope to be added to the stack
     */
    protected void addScope(DelphiScope newScope) {
      ((DelphiScopeImpl) newScope).setParent(currentScope());
      scopes.push(newScope);
    }

    protected void addDeclaration(NameDeclaration declaration, NameDeclarationNode node) {
      handleImplementationDeclaration(declaration, node);
      registerDeclaration(declaration, node);
      ((NameDeclarationNodeImpl) node).setNameDeclaration(declaration);
      addDeclarationToCurrentScope(declaration);
    }

    protected void addDeclaration(MethodNameDeclaration declaration, MethodNameNode node) {
      handleImplementationDeclaration(declaration, node);
      registerDeclaration(declaration, node);
      node.setMethodNameDeclaration(declaration);
      addDeclarationToCurrentScope(declaration);
    }

    protected void registerDeclaration(NameDeclaration declaration, DelphiNode node) {
      unitDeclaration.getFileScope().registerDeclaration(node, declaration);
    }

    protected void addDeclarationToCurrentScope(NameDeclaration declaration) {
      ((DelphiScopeImpl) currentScope()).addDeclaration(declaration);
    }

    protected UnitImportNameDeclaration resolveImport(UnitImportNode node) {
      return importHandler.resolveImport(unitDeclaration, node);
    }
  }

  public static SymbolTableVisitor interfaceVisitor() {
    return new SymbolTableVisitor() {
      @Override
      public Data visit(DelphiAst node, Data data) {
        FileHeaderNode header = node.getFileHeader();
        if (!(header instanceof UnitDeclarationNode)) {
          // Only units have interface sections.
          return data;
        }

        return super.visit(node, data);
      }

      @Override
      public Data visit(ImplementationSectionNode node, Data data) {
        return data;
      }

      @Override
      public Data visit(InitializationSectionNode node, Data data) {
        return data;
      }

      @Override
      public Data visit(FinalizationSectionNode node, Data data) {
        return data;
      }
    };
  }

  public static SymbolTableVisitor implementationVisitor() {
    return new SymbolTableVisitor() {
      @Override
      public Data visit(InterfaceSectionNode node, Data data) {
        return data;
      }
    };
  }

  @Override
  public Data visit(DelphiAst node, Data data) {
    if (!data.scopes.isEmpty()) {
      ((MutableDelphiNode) node).setScope(data.currentScope());
      return visitScope(node, data);
    }

    return createUnitScope(node, data);
  }

  @Override
  public Data visit(UnitImportNode node, Data data) {
    UnitImportNameDeclaration declaration = data.resolveImport(node);
    data.addDeclaration(declaration, node.getNameNode());
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(TypeDeclarationNode node, Data data) {
    return createTypeScope(node, data);
  }

  @Override
  public Data visit(MethodResolutionClauseNode node, Data data) {
    data.nameResolutionHelper.resolve(node);
    return data;
  }

  @Override
  public Data visit(EnumElementNode node, Data data) {
    EnumElementNameDeclaration declaration = new EnumElementNameDeclarationImpl(node);
    data.addDeclaration(declaration, node.getNameDeclarationNode());

    if (!data.switchRegistry.isActiveSwitch(SwitchKind.SCOPEDENUMS, node.getTokenIndex())) {
      DelphiScope currentScope = data.currentScope();

      DelphiScope scope = currentScope.getEnclosingScope(MethodScope.class);
      if (scope == null) {
        scope = currentScope.getEnclosingScope(FileScope.class);
      }

      if (currentScope != scope) {
        ((DelphiScopeImpl) scope).addDeclaration(declaration);
      }
    }

    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(CompoundStatementNode node, Data data) {
    // top-level blocks for methods should have the same scope as parameters, just skip them
    // same applies to catch statements defining exceptions + the catch block, and for-blocks
    if (node.getParent() instanceof MethodBodyNode
        || node.getParent() instanceof ExceptItemNode
        || node.getParent() instanceof ForStatementNode) {
      return DelphiParserVisitor.super.visit(node, data);
    }
    return createLocalScope(node, data);
  }

  @Override
  public Data visit(ExceptItemNode node, Data data) {
    data.addScope(new LocalScopeImpl(), node);
    data.nameResolutionHelper.resolve(node.getExceptionType());
    return visitScope(node, data);
  }

  @Override
  public Data visit(MethodDeclarationNode node, Data data) {
    DeclarationScope scope = new DeclarationScopeImpl();
    data.addScope(scope);
    ((MutableDelphiNode) node).setScope(scope);

    NameDeclarationNode nameNode = node.getMethodNameNode().getNameDeclarationNode();
    createTypeParameterDeclarations(nameNode.getGenericDefinition(), data);
    data.nameResolutionHelper.resolve(node);

    visitScope(node, data);
    ((MutableDelphiNode) node).setScope(null);

    MethodNameDeclaration declaration = MethodNameDeclarationImpl.create(node, data.typeFactory);
    data.addDeclaration(declaration, node.getMethodNameNode().getNameDeclarationNode());
    return data;
  }

  private static void createTypeParameterDeclarations(
      @Nullable GenericDefinitionNode definition, Data data) {
    if (definition != null) {
      for (TypeParameterNode parameterNode : definition.getTypeParameterNodes()) {
        parameterNode.getTypeConstraintNodes().forEach(data.nameResolutionHelper::resolve);
      }

      for (TypeParameter typeParameter : definition.getTypeParameters()) {
        NameDeclarationNode location = typeParameter.getLocation();
        var declaration = new TypeParameterNameDeclarationImpl(location, typeParameter.getType());
        data.addDeclaration(declaration, location);
      }
    }
  }

  @Override
  public Data visit(MethodImplementationNode node, Data data) {
    MethodScopeImpl scope = new MethodScopeImpl();
    scope.setParent(data.currentScope());
    ((MutableDelphiNode) node).setScope(scope);

    data.nameResolutionHelper.resolve(node);

    NameReferenceNode methodReference = node.getNameReferenceNode();
    NameDeclaration declaration = methodReference.getLastName().getNameDeclaration();

    MethodNameDeclaration methodDeclaration = null;
    boolean foundInterfaceDeclaration = false;
    boolean qualifiedMethodName = methodReference.flatten().size() > 1;

    if (declaration instanceof MethodNameDeclaration) {
      foundInterfaceDeclaration = true;
      methodDeclaration = (MethodNameDeclaration) declaration;
    }

    if (!foundInterfaceDeclaration && !qualifiedMethodName) {
      methodDeclaration = MethodNameDeclarationImpl.create(node, data.typeFactory);
      MethodNameNode nameNode = node.getMethodHeading().getMethodNameNode();
      data.addDeclaration(methodDeclaration, nameNode);
    }

    scope.setMethodNameDeclaration(methodDeclaration);

    data.addScope(scope, node);

    if (foundInterfaceDeclaration
        && node.getMethodHeading().getMethodParametersNode() == null
        && !node.hasDirective(MethodDirective.EXTERNAL)) {
      methodDeclaration.getParameters().stream()
          .map(parameter -> compilerVariable(parameter.getImage(), parameter.getType(), scope))
          .forEach(data::addDeclarationToCurrentScope);
    }

    if (node.isFunction() || node.isOperator()) {
      Type resultType = findResultType(node, methodDeclaration);
      NameDeclaration result = compilerVariable("Result", resultType, scope);
      data.addDeclarationToCurrentScope(result);
    }

    boolean hasSelfParameter = false;

    MethodParametersNode parametersNode = node.getMethodHeading().getMethodParametersNode();
    if (parametersNode != null) {
      hasSelfParameter =
          parametersNode.getParameters().stream()
              .map(FormalParameterData::getImage)
              .anyMatch(image -> image.equalsIgnoreCase("Self"));
    }

    if (!hasSelfParameter) {
      Type selfType = findSelfType(node, methodDeclaration, data);
      if (selfType != null) {
        NameDeclaration self = compilerVariable("Self", selfType, scope);
        data.addDeclarationToCurrentScope(self);
      }
    }

    return visitScope(node, data);
  }

  private static Type findResultType(MethodNode node, @Nullable MethodNameDeclaration declaration) {
    if (declaration == null) {
      return node.getReturnType();
    } else {
      return declaration.getReturnType();
    }
  }

  private static Type findSelfType(
      MethodNode node, @Nullable MethodNameDeclaration declaration, Data data) {
    Type selfType = null;
    TypeNameDeclaration methodType = node.getTypeDeclaration();
    if (methodType != null) {
      selfType = methodType.getType();
      if (selfType.isHelper()) {
        selfType = ((HelperType) selfType).extendedType();
      }
      if (node.isClassMethod()) {
        selfType = data.typeFactory.classOf(null, selfType);
        if (declaration != null && declaration.hasDirective(MethodDirective.STATIC)) {
          selfType = null;
        }
      }
    }
    return selfType;
  }

  @Override
  public Data visit(PropertyNode node, Data data) {
    data.nameResolutionHelper.resolve(node);

    PropertyNameDeclaration concreteDeclaration = findConcretePropertyDeclaration(node);

    var declaration = new PropertyNameDeclarationImpl(node, concreteDeclaration);
    data.addDeclaration(declaration, node.getPropertyName());

    if (concreteDeclaration != null) {
      ((PropertyNameDeclarationImpl) concreteDeclaration).addRedeclaration(declaration);
    }

    return createDeclarationScope(node, data);
  }

  @Nullable
  private static PropertyNameDeclaration findConcretePropertyDeclaration(PropertyNode property) {
    if (!property.getType().isUnknown()) {
      // This is already a concrete declaration
      return null;
    }

    PropertyNameDeclaration result = null;
    DelphiScope scope = property.getScope();

    var imaginaryLocation = SymbolicNode.imaginary(property.getPropertyName().getImage(), scope);
    var occurrence = new NameOccurrenceImpl(imaginaryLocation);

    while (result == null && scope instanceof TypeScope) {
      scope = ((TypeScope) scope).getSuperTypeScope();
      if (scope != null) {
        NameDeclaration declaration = Iterables.getFirst(scope.findDeclaration(occurrence), null);
        if (declaration instanceof PropertyNameDeclaration) {
          result = ((PropertyNameDeclaration) declaration);
        }
      }
    }

    return result;
  }

  @Override
  public Data visit(ProcedureTypeHeadingNode node, Data data) {
    return createDeclarationScope(node, data);
  }

  @Override
  public Data visit(AnonymousMethodNode node, Data data) {
    MethodScope scope = new MethodScopeImpl();
    data.addScope(scope, node);
    data.nameResolutionHelper.resolve(node.getMethodParametersNode());
    data.nameResolutionHelper.resolve(node.getReturnTypeNode());
    if (node.isFunction()) {
      NameDeclaration result = compilerVariable("Result", node.getReturnType(), scope);
      data.addDeclarationToCurrentScope(result);
    }
    return visitScope(node, data);
  }

  @Override
  public Data visit(TryStatementNode node, Data data) {
    return createLocalScope(node, data);
  }

  @Override
  public Data visit(ForToStatementNode node, Data data) {
    data.addScope(new LocalScopeImpl(), node);

    // Visit the initializer expression first - we may need the expression type for type inference.
    node.getInitializerExpression().accept(this, data);
    node.getTargetExpression().accept(this, data);
    node.getVariable().accept(this, data);

    return visitScope(node.getStatement(), data);
  }

  @Override
  public Data visit(ForInStatementNode node, Data data) {
    data.addScope(new LocalScopeImpl(), node);

    // Visit the enumerable expression first - we may need the expression type for type inference.
    node.getEnumerable().accept(this, data);
    node.getVariable().accept(this, data);

    return visitScope(node.getStatement(), data);
  }

  @Override
  public Data visit(ForLoopVarReferenceNode node, Data data) {
    data.nameResolutionHelper.resolve(node.getNameReference());
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(RepeatStatementNode node, Data data) {
    return createLocalScope(node, data);
  }

  @Override
  public Data visit(WithStatementNode node, Data data) {
    ((WithStatementNodeImpl) node).setScope(data.currentScope());

    DelphiScope parent = data.currentScope();
    for (ExpressionNode target : node.getTargets()) {
      target.accept(this, data);
      WithScopeImpl withScope = new WithScopeImpl(getTargetScope(target));
      withScope.setParent(parent);
      ((WithStatementNodeImpl) node).setScope(withScope);
      parent = withScope;
    }

    LocalScopeImpl scope = new LocalScopeImpl();
    data.addScope(scope, node);
    scope.setParent(parent);

    return visitScope(node.getStatement(), data);
  }

  private static DelphiScope getTargetScope(ExpressionNode target) {
    Type targetType = target.getType();

    if (targetType.isProcedural()) {
      targetType = ((ProceduralType) targetType).returnType();
    }

    targetType = TypeUtils.findBaseType(targetType);

    if (targetType instanceof ScopedType) {
      return ((ScopedType) targetType).typeScope();
    }

    return DelphiScope.unknownScope();
  }

  @Override
  public Data visit(CaseStatementNode node, Data data) {
    return createLocalScope(node, data);
  }

  @Override
  public Data visit(ConstStatementNode node, Data data) {
    return handleVarDeclaration(node, node.getTypeNode(), data);
  }

  @Override
  public Data visit(VarStatementNode node, Data data) {
    return handleVarDeclaration(node, node.getTypeNode(), data);
  }

  @Override
  public Data visit(VarDeclarationNode node, Data data) {
    return handleVarDeclaration(node, node.getTypeNode(), data);
  }

  @Override
  public Data visit(FieldDeclarationNode node, Data data) {
    return handleVarDeclaration(node, node.getTypeNode(), data);
  }

  @Override
  public Data visit(RecordVariantTagNode node, Data data) {
    return handleVarDeclaration(node, node.getTypeNode(), data);
  }

  @Override
  public Data visit(ForLoopVarDeclarationNode node, Data data) {
    return handleVarDeclaration(node, node.getTypeNode(), data);
  }

  @Override
  public Data visit(ConstDeclarationNode node, Data data) {
    return handleVarDeclaration(node, node.getTypeNode(), data);
  }

  private Data handleVarDeclaration(DelphiNode declarationNode, TypeNode typeNode, Data data) {
    if (typeNode instanceof RecordTypeNode || typeNode instanceof EnumTypeNode) {
      createAnonymousTypeScope(typeNode, data);
    } else if (typeNode != null) {
      data.nameResolutionHelper.resolve(typeNode);
    }

    for (int i = declarationNode.getChildren().size() - 1; i >= 0; --i) {
      DelphiNode child = declarationNode.getChild(i);
      if (!(child instanceof TypeNode)) {
        child.accept(this, data);
      }
    }

    return data;
  }

  @Override
  public Data visit(NameDeclarationNode node, Data data) {
    if (isVariableNameDeclaration(node)) {
      VariableNameDeclaration declaration = new VariableNameDeclarationImpl(node);
      data.addDeclaration(declaration, node);
    }
    return DelphiParserVisitor.super.visit(node, data);
  }

  private static boolean isVariableNameDeclaration(NameDeclarationNode node) {
    switch (node.getKind()) {
      case CONST:
      case EXCEPT_ITEM:
      case FIELD:
      case INLINE_CONST:
      case INLINE_VAR:
      case LOOP_VAR:
      case PARAMETER:
      case RECORD_VARIANT_TAG:
      case VAR:
        return true;
      default:
        return false;
    }
  }

  @Override
  public Data visit(PrimaryExpressionNode node, Data data) {
    node.findDescendantsOfType(AnonymousMethodNode.class)
        .forEach(method -> this.visit(method, data));
    node.findDescendantsOfType(ArrayConstructorNode.class)
        .forEach(
            arrayConstructor ->
                arrayConstructor.getElements().forEach(element -> element.accept(this, data)));
    data.nameResolutionHelper.resolve(node);
    return data;
  }

  @Override
  public Data visit(TypeSectionNode node, Data data) {
    DelphiParserVisitor.super.visit(node, data);

    for (DelphiNode child : node.getChildren()) {
      TypeDeclarationNode typeDeclaration = (TypeDeclarationNode) child;
      if (typeDeclaration.isClassReference()) {
        var classReference = (ClassReferenceTypeNode) typeDeclaration.getTypeNode();
        TypeNode classOf = classReference.getClassOfTypeNode();
        data.nameResolutionHelper.resolve(classOf);
        ((ClassReferenceTypeImpl) classReference.getType()).setClassType(classOf.getType());
      } else if (typeDeclaration.isPointer()) {
        var pointer = (PointerTypeNode) typeDeclaration.getTypeNode();
        TypeNode dereferenced = pointer.getDereferencedTypeNode();
        data.nameResolutionHelper.resolve(dereferenced);
        ((PointerTypeImpl) pointer.getType()).setDereferencedType(dereferenced.getType());
      }
    }

    return data;
  }

  private Data createDeclarationScope(DelphiNode node, Data data) {
    data.addScope(new DeclarationScopeImpl());
    return visitScope(node, data);
  }

  private Data createLocalScope(DelphiNode node, Data data) {
    data.addScope(new LocalScopeImpl(), node);
    return visitScope(node, data);
  }

  private Data createTypeScope(TypeDeclarationNode node, Data data) {
    TypeScopeImpl typeScope = new TypeScopeImpl();
    data.addScope(typeScope, node.getTypeNode());

    NameDeclarationNode typeNameNode = node.getTypeNameNode();
    createTypeParameterDeclarations(typeNameNode.getGenericDefinition(), data);
    if (!node.isPointer() && !node.isClassReference()) {
      data.nameResolutionHelper.resolve(node);
    }

    TypeNameDeclaration declaration = new TypeNameDeclarationImpl(node);
    handleImplementationDeclaration(declaration, node);
    data.registerDeclaration(declaration, typeNameNode);
    ((NameDeclarationNodeImpl) typeNameNode).setNameDeclaration(declaration);

    Type type = declaration.getType();
    typeScope.setType(type);

    if (!node.isTypeAlias()
        && type.isPointer()
        && data.switchRegistry.isActiveSwitch(SwitchKind.POINTERMATH, node.getTokenIndex())) {
      ((PointerTypeImpl) type).setAllowsPointerMath();
    }

    DelphiScope parent = Objects.requireNonNull(data.currentScope().getParent());
    ((DelphiScopeImpl) parent).addDeclaration(declaration);

    return visitScope(node, data);
  }

  private void createAnonymousTypeScope(TypeNode node, Data data) {
    data.addScope(new TypeScopeImpl(), node);
    visitScope(node, data);
  }

  private Data createUnitScope(DelphiAst node, Data data) {
    FileHeaderNode fileHeader = node.getFileHeader();
    String name = fileHeader.getName();
    FileScope fileScope;

    if (name.equals("System")) {
      fileScope = new SystemScopeImpl(data.typeFactory);
    } else if (name.equals("SysInit")) {
      fileScope = new SysInitScopeImpl(name, data.systemScope);
    } else {
      fileScope = new UnitScopeImpl(name, data.systemScope, data.sysInitScope);
    }

    data.scopes.add(fileScope);
    ((DelphiAstImpl) node).setScope(fileScope);
    data.unitDeclaration = new UnitNameDeclarationImpl(fileHeader, fileScope);
    data.addDeclaration(data.unitDeclaration, fileHeader.getNameNode());

    return visitScope(node, data);
  }

  private Data visitScope(DelphiNode node, Data data) {
    if (node != null) {
      DelphiParserVisitor.super.visit(node, data);
    }
    data.scopes.pop();
    return data;
  }

  private static void handleImplementationDeclaration(
      NameDeclaration declaration, DelphiNode node) {
    if (node.getFirstParentOfType(ImplementationSectionNode.class) != null) {
      declaration.setIsImplementationDeclaration();
    }
  }
}
