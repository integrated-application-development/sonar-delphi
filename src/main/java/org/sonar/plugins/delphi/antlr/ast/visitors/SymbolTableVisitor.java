package org.sonar.plugins.delphi.antlr.ast.visitors;

import static com.google.common.collect.Iterables.getFirst;
import static org.sonar.plugins.delphi.preprocessor.directive.CompilerDirectiveType.POINTERMATH;
import static org.sonar.plugins.delphi.preprocessor.directive.CompilerDirectiveType.SCOPEDENUMS;
import static org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration.compilerVariable;

import com.google.common.base.Preconditions;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.AnonymousMethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.ArrayConstructorNode;
import org.sonar.plugins.delphi.antlr.ast.node.CaseStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ClassReferenceTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ConstDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.ConstStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.EnumElementNode;
import org.sonar.plugins.delphi.antlr.ast.node.EnumTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExceptItemNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.FieldDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.FileHeaderNode;
import org.sonar.plugins.delphi.antlr.ast.node.FinalizationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForInStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForLoopVarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForLoopVarReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForToStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.GenericDefinitionNode;
import org.sonar.plugins.delphi.antlr.ast.node.GenericDefinitionNode.TypeParameter;
import org.sonar.plugins.delphi.antlr.ast.node.ImplementationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InitializationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InterfaceSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNameNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodResolutionClauseNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PointerTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ProcedureTypeHeadingNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyNode;
import org.sonar.plugins.delphi.antlr.ast.node.RecordTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.RecordVariantTagNode;
import org.sonar.plugins.delphi.antlr.ast.node.RepeatStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.TryStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeParameterNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.WithStatementNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.Data;
import org.sonar.plugins.delphi.preprocessor.CompilerSwitchRegistry;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.ImportResolutionHandler;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.EnumElementNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodDirective;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeParameterNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.symbol.resolve.NameResolutionHelper;
import org.sonar.plugins.delphi.symbol.scope.DeclarationScope;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.FileScope;
import org.sonar.plugins.delphi.symbol.scope.LocalScope;
import org.sonar.plugins.delphi.symbol.scope.MethodScope;
import org.sonar.plugins.delphi.symbol.scope.SysInitScope;
import org.sonar.plugins.delphi.symbol.scope.SystemScope;
import org.sonar.plugins.delphi.symbol.scope.TypeScope;
import org.sonar.plugins.delphi.symbol.scope.UnitScope;
import org.sonar.plugins.delphi.symbol.scope.WithScope;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ClassReferenceType;
import org.sonar.plugins.delphi.type.Type.HelperType;
import org.sonar.plugins.delphi.type.Type.PointerType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.ScopedType;
import org.sonar.plugins.delphi.type.TypeUtils;
import org.sonar.plugins.delphi.type.factory.TypeFactory;

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
      node.setScope(newScope);
    }

    /**
     * Add a new scope to the stack
     *
     * @param newScope the scope to be added to the stack
     */
    protected void addScope(DelphiScope newScope) {
      newScope.setParent(currentScope());
      scopes.push(newScope);
    }

    protected void addDeclaration(DelphiNameDeclaration declaration, NameDeclarationNode node) {
      handleImplementationDeclaration(declaration, node);
      registerDeclaration(declaration, node);
      node.setNameDeclaration(declaration);
      addDeclarationToCurrentScope(declaration);
    }

    protected void addDeclaration(MethodNameDeclaration declaration, MethodNameNode node) {
      handleImplementationDeclaration(declaration, node);
      registerDeclaration(declaration, node);
      node.setMethodNameDeclaration(declaration);
      addDeclarationToCurrentScope(declaration);
    }

    protected void registerDeclaration(DelphiNameDeclaration declaration, DelphiNode node) {
      unitDeclaration.getFileScope().registerDeclaration(node, declaration);
    }

    protected void addDeclarationToCurrentScope(DelphiNameDeclaration declaration) {
      currentScope().addDeclaration(declaration);
    }

    protected UnitImportNameDeclaration resolveImport(UnitImportNode node) {
      return importHandler.resolveImport(unitDeclaration, node);
    }
  }

  public static SymbolTableVisitor interfaceVisitor() {
    return new SymbolTableVisitor() {
      @Override
      public Data visit(DelphiAST node, Data data) {
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
  public Data visit(DelphiAST node, Data data) {
    if (!data.scopes.isEmpty()) {
      node.setScope(data.currentScope());
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
    EnumElementNameDeclaration declaration = new EnumElementNameDeclaration(node);
    data.addDeclaration(declaration, node.getNameDeclarationNode());

    if (!data.switchRegistry.isActiveSwitch(SCOPEDENUMS, node.getTokenIndex())) {
      DelphiScope currentScope = data.currentScope();

      DelphiScope scope = currentScope.getEnclosingScope(MethodScope.class);
      if (scope == null) {
        scope = currentScope.getEnclosingScope(FileScope.class);
      }

      if (currentScope != scope) {
        scope.addDeclaration(declaration);
      }
    }

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
    data.addScope(new LocalScope(), node);
    data.nameResolutionHelper.resolve(node.getExceptionType());
    return visitScope(node, data);
  }

  @Override
  public Data visit(MethodDeclarationNode node, Data data) {
    DeclarationScope scope = new DeclarationScope();
    data.addScope(scope);
    node.setScope(scope);

    NameDeclarationNode nameNode = node.getMethodNameNode().getNameDeclarationNode();
    createTypeParameterDeclarations(nameNode.getGenericDefinition(), data);
    data.nameResolutionHelper.resolve(node);

    visitScope(node, data);
    node.setScope(null);

    MethodNameDeclaration declaration = MethodNameDeclaration.create(node, data.typeFactory);
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
        var declaration = new TypeParameterNameDeclaration(location, typeParameter.getType());
        data.addDeclaration(declaration, location);
      }
    }
  }

  @Override
  public Data visit(MethodImplementationNode node, Data data) {
    MethodScope scope = new MethodScope();
    scope.setParent(data.currentScope());
    node.setScope(scope);

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
      methodDeclaration = MethodNameDeclaration.create(node, data.typeFactory);
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
      DelphiNameDeclaration result = compilerVariable("Result", resultType, scope);
      data.addDeclarationToCurrentScope(result);
    }

    Type selfType = findSelfType(node, methodDeclaration, data);
    if (selfType != null) {
      DelphiNameDeclaration self = compilerVariable("Self", selfType, scope);
      data.addDeclarationToCurrentScope(self);
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
        selfType = data.typeFactory.classOf(selfType);
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

    var declaration = new PropertyNameDeclaration(node, findConcretePropertyDeclaration(node));
    data.addDeclaration(declaration, node.getPropertyName());

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
    var occurrence = new DelphiNameOccurrence(imaginaryLocation);

    while (result == null && scope instanceof TypeScope) {
      scope = ((TypeScope) scope).getSuperTypeScope();
      if (scope != null) {
        NameDeclaration declaration = getFirst(scope.findDeclaration(occurrence), null);
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
    MethodScope scope = new MethodScope();
    data.addScope(scope, node);
    data.nameResolutionHelper.resolve(node.getMethodParametersNode());
    data.nameResolutionHelper.resolve(node.getReturnTypeNode());
    if (node.isFunction()) {
      DelphiNameDeclaration result = compilerVariable("Result", node.getReturnType(), scope);
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
    data.addScope(new LocalScope(), node);

    // Visit the initializer expression first - we may need the expression type for type inference.
    node.getInitializerExpression().accept(this, data);
    node.getTargetExpression().accept(this, data);
    node.getVariable().accept(this, data);

    return visitScope(node.getStatement(), data);
  }

  @Override
  public Data visit(ForInStatementNode node, Data data) {
    data.addScope(new LocalScope(), node);

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
    node.setScope(data.currentScope());

    DelphiScope parent = data.currentScope();
    for (ExpressionNode target : node.getTargets()) {
      target.accept(this, data);
      WithScope withScope = new WithScope(getTargetScope(target));
      withScope.setParent(parent);
      node.setScope(withScope);
      parent = withScope;
    }

    LocalScope scope = new LocalScope();
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

    for (int i = declarationNode.jjtGetNumChildren() - 1; i >= 0; --i) {
      DelphiNode child = (DelphiNode) declarationNode.jjtGetChild(i);
      if (!(child instanceof TypeNode)) {
        child.accept(this, data);
      }
    }

    return data;
  }

  @Override
  public Data visit(NameDeclarationNode node, Data data) {
    if (isVariableNameDeclaration(node)) {
      VariableNameDeclaration declaration = new VariableNameDeclaration(node);
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

    for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
      TypeDeclarationNode typeDeclaration = (TypeDeclarationNode) node.jjtGetChild(i);
      if (typeDeclaration.isClassReference()) {
        var classReference = (ClassReferenceTypeNode) typeDeclaration.getTypeNode();
        TypeNode classOf = classReference.getClassOfTypeNode();
        data.nameResolutionHelper.resolve(classOf);
        ((ClassReferenceType) classReference.getType()).setClassType(classOf.getType());
      } else if (typeDeclaration.isPointer()) {
        var pointer = (PointerTypeNode) typeDeclaration.getTypeNode();
        TypeNode dereferenced = pointer.getDereferencedTypeNode();
        data.nameResolutionHelper.resolve(dereferenced);
        ((PointerType) pointer.getType()).setDereferencedType(dereferenced.getType());
      }
    }

    return data;
  }

  private Data createDeclarationScope(DelphiNode node, Data data) {
    data.addScope(new DeclarationScope());
    return visitScope(node, data);
  }

  private Data createLocalScope(DelphiNode node, Data data) {
    data.addScope(new LocalScope(), node);
    return visitScope(node, data);
  }

  private Data createTypeScope(TypeDeclarationNode node, Data data) {
    TypeScope typeScope = new TypeScope();
    data.addScope(typeScope, node.getTypeNode());

    NameDeclarationNode typeNameNode = node.getTypeNameNode();
    createTypeParameterDeclarations(typeNameNode.getGenericDefinition(), data);
    if (!node.isPointer() && !node.isClassReference()) {
      data.nameResolutionHelper.resolve(node);
    }

    TypeNameDeclaration declaration = new TypeNameDeclaration(node);
    handleImplementationDeclaration(declaration, node);
    data.registerDeclaration(declaration, typeNameNode);
    typeNameNode.setNameDeclaration(declaration);

    Type type = declaration.getType();
    typeScope.setType(type);

    if (!node.isTypeAlias()
        && type.isPointer()
        && data.switchRegistry.isActiveSwitch(POINTERMATH, node.getTokenIndex())) {
      ((PointerType) type).setAllowsPointerMath();
    }

    DelphiScope parent = Objects.requireNonNull(data.currentScope().getParent());
    parent.addDeclaration(declaration);

    return visitScope(node, data);
  }

  private void createAnonymousTypeScope(TypeNode node, Data data) {
    data.addScope(new TypeScope(), node);
    visitScope(node, data);
  }

  private Data createUnitScope(DelphiAST node, Data data) {
    FileHeaderNode fileHeader = node.getFileHeader();
    String name = fileHeader.getName();
    FileScope fileScope;

    if (name.equals("System")) {
      fileScope = new SystemScope(data.typeFactory);
    } else if (name.equals("SysInit")) {
      fileScope = new SysInitScope(name, data.systemScope);
    } else {
      fileScope = new UnitScope(name, data.systemScope, data.sysInitScope);
    }

    data.scopes.add(fileScope);
    node.setScope(fileScope);
    data.unitDeclaration = new UnitNameDeclaration(fileHeader, fileScope);
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
      DelphiNameDeclaration declaration, DelphiNode node) {
    if (node.isImplementationSection()) {
      declaration.setIsImplementationDeclaration();
    }
  }
}
