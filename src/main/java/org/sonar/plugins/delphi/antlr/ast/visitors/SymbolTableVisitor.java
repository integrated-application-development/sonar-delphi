package org.sonar.plugins.delphi.antlr.ast.visitors;

import static com.google.common.collect.Iterables.getFirst;
import static org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.ResolutionLevel.COMPLETE;
import static org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.ResolutionLevel.NONE;
import static org.sonar.plugins.delphi.preprocessor.directive.CompilerDirectiveType.SCOPED_ENUMS;
import static org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration.compilerVariable;
import static org.sonar.plugins.delphi.symbol.resolve.NameResolver.resolve;

import com.google.common.base.Preconditions;
import java.util.ArrayDeque;
import java.util.Deque;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.AnonymousMethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.ArrayConstructorNode;
import org.sonar.plugins.delphi.antlr.ast.node.CaseStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ClassReferenceTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ConstDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.EnumElementNode;
import org.sonar.plugins.delphi.antlr.ast.node.EnumTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExceptItemNode;
import org.sonar.plugins.delphi.antlr.ast.node.FieldDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.FileHeaderNode;
import org.sonar.plugins.delphi.antlr.ast.node.FinalizationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.GenericDefinitionNode;
import org.sonar.plugins.delphi.antlr.ast.node.GenericDefinitionNode.TypeParameter;
import org.sonar.plugins.delphi.antlr.ast.node.ImplementationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InitializationSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.InterfaceSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNameNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodResolutionClauseNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PointerTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ProcedureTypeHeadingNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyNode;
import org.sonar.plugins.delphi.antlr.ast.node.RecordTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.RepeatStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.TryStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeParameterNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeSectionNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitImportNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.WithStatementNode;
import org.sonar.plugins.delphi.antlr.ast.visitors.SymbolTableVisitor.Data;
import org.sonar.plugins.delphi.preprocessor.CompilerSwitchRegistry;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.ImportResolutionHandler;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.EnumElementNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeParameterNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.symbol.resolve.NameResolver;
import org.sonar.plugins.delphi.symbol.scope.DeclarationScope;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.FileScope;
import org.sonar.plugins.delphi.symbol.scope.LocalScope;
import org.sonar.plugins.delphi.symbol.scope.MethodScope;
import org.sonar.plugins.delphi.symbol.scope.SystemScope;
import org.sonar.plugins.delphi.symbol.scope.TypeScope;
import org.sonar.plugins.delphi.symbol.scope.UnitScope;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ClassReferenceType;
import org.sonar.plugins.delphi.type.Type.HelperType;
import org.sonar.plugins.delphi.type.Type.PointerType;

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
 * accordingly. (Delegated to the {@link NameResolver})
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
    private final CompilerSwitchRegistry switchRegistry;
    private final SystemScope systemScope;
    private final Deque<DelphiScope> scopes;
    private UnitNameDeclaration unitDeclaration;

    public Data(
        ResolutionLevel resolved,
        ResolutionLevel resolutionLevel,
        ImportResolutionHandler importHandler,
        CompilerSwitchRegistry switchRegistry,
        @Nullable SystemScope systemScope,
        @Nullable UnitNameDeclaration unitDeclaration) {
      this.resolved = resolved;
      this.resolutionLevel = resolutionLevel;
      this.importHandler = importHandler;
      this.switchRegistry = switchRegistry;
      this.systemScope = systemScope;
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
     * Add a new scope to the stack
     *
     * @param newScope the scope to be added to the stack
     */
    private void addScope(DelphiScope newScope) {
      newScope.setParent(currentScope());
      scopes.push(newScope);
    }

    private void addDeclaration(DelphiNameDeclaration declaration, NameDeclarationNode node) {
      registerDeclaration(declaration, node);
      node.setNameDeclaration(declaration);
      addDeclarationToCurrentScope(declaration);
    }

    private void addDeclaration(MethodNameDeclaration declaration, MethodNameNode node) {
      registerDeclaration(declaration, node);
      node.setMethodNameDeclaration(declaration);
      addDeclarationToCurrentScope(declaration);
    }

    private void registerDeclaration(DelphiNameDeclaration declaration, DelphiNode node) {
      unitDeclaration.getUnitScope().registerDeclaration(node, declaration);
    }

    private void addDeclarationToCurrentScope(DelphiNameDeclaration declaration) {
      currentScope().addDeclaration(declaration);
    }

    private UnitImportNameDeclaration resolveImport(UnitImportNode node) {
      return importHandler.resolveImport(unitDeclaration, node);
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

  private Data createTypeScope(TypeDeclarationNode node, Data data) {
    TypeScope typeScope = new TypeScope();
    data.addScope(typeScope, node.getTypeNode());

    NameDeclarationNode typeNameNode = node.getTypeNameNode();
    createTypeParameterDeclarations(typeNameNode.getGenericDefinition(), data);
    resolve(node);

    TypeNameDeclaration declaration = new TypeNameDeclaration(node);
    data.registerDeclaration(declaration, typeNameNode);
    typeNameNode.setNameDeclaration(declaration);
    typeScope.setType(declaration.getType());

    DelphiScope parent = Preconditions.checkNotNull(data.currentScope().getParent());
    parent.addDeclaration(declaration);

    if (declaration.getType().isEnum()
        && data.switchRegistry.isActiveSwitch(SCOPED_ENUMS, node.getTokenIndex())) {
      declaration.setIsScopedEnum();
    }

    return visitScope(node, data);
  }

  private void createAnonymousTypeScope(TypeNode node, Data data) {
    data.addScope(new TypeScope(), node);
    visitScope(node, data);
  }

  private Data createUnitScope(DelphiAST node, Data data) {
    FileHeaderNode fileHeader = node.getFileHeader();
    String name = fileHeader.getName();
    boolean system = name.equals("System");

    FileScope unitScope = system ? new SystemScope() : new UnitScope(name, data.systemScope);
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
    if (data.resolved == COMPLETE || data.resolutionLevel == NONE) {
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
    if (data.resolved != NONE) {
      return data;
    }
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(ImplementationSectionNode node, Data data) {
    if (data.resolved == COMPLETE || data.resolutionLevel != COMPLETE) {
      return data;
    }
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(InitializationSectionNode node, Data data) {
    if (data.resolved == COMPLETE || data.resolutionLevel != COMPLETE) {
      return data;
    }
    return DelphiParserVisitor.super.visit(node, data);
  }

  @Override
  public Data visit(FinalizationSectionNode node, Data data) {
    if (data.resolved == COMPLETE || data.resolutionLevel != COMPLETE) {
      return data;
    }
    return DelphiParserVisitor.super.visit(node, data);
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
    resolve(node);
    return data;
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
    DeclarationScope scope = new DeclarationScope();
    data.addScope(scope);
    node.setScope(scope);

    NameDeclarationNode nameNode = node.getMethodNameNode().getNameDeclarationNode();
    createTypeParameterDeclarations(nameNode.getGenericDefinition(), data);
    resolve(node);

    visitScope(node, data);
    node.setScope(null);

    MethodNameDeclaration declaration = MethodNameDeclaration.create(node);
    data.addDeclaration(declaration, node.getMethodNameNode().getNameDeclarationNode());
    return data;
  }

  private static void createTypeParameterDeclarations(
      @Nullable GenericDefinitionNode definition, Data data) {
    if (definition != null) {
      for (TypeParameterNode parameterNode : definition.getTypeParameterNodes()) {
        parameterNode.getTypeConstraintNodes().forEach(NameResolver::resolve);
      }

      for (TypeParameter typeParameter : definition.getTypeParameters()) {
        NameDeclarationNode location = typeParameter.getLocation();
        var declaration = new TypeParameterNameDeclaration(location, typeParameter.getType());
        data.addDeclaration(declaration, location);
      }
    }
  }

  @Override
  public Data visit(PropertyNode node, Data data) {
    resolve(node);

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
  public Data visit(MethodImplementationNode node, Data data) {
    MethodScope methodScope = new MethodScope(node.fullyQualifiedName());
    methodScope.setParent(data.currentScope());
    node.setScope(methodScope);

    resolve(node);

    NameReferenceNode methodReference = node.getNameReferenceNode();
    NameDeclaration declaration = methodReference.getLastName().getNameDeclaration();
    boolean foundInterfaceDeclaration = declaration instanceof MethodNameDeclaration;
    boolean qualifiedMethodName = methodReference.flatten().size() > 1;

    if (!foundInterfaceDeclaration && !qualifiedMethodName) {
      MethodNameDeclaration implementationDeclaration = MethodNameDeclaration.create(node);
      MethodNameNode nameNode = node.getMethodHeading().getMethodNameNode();
      data.addDeclaration(implementationDeclaration, nameNode);
    }

    data.addScope(methodScope, node);

    if (node.isFunction() || node.isOperator()) {
      DelphiNameDeclaration result = compilerVariable("Result", node.getReturnType(), methodScope);
      data.addDeclarationToCurrentScope(result);
    }

    Type selfType = findSelfType(node);
    if (selfType != null) {
      DelphiNameDeclaration self = compilerVariable("Self", selfType, methodScope);
      data.addDeclarationToCurrentScope(self);
    }

    return visitScope(node, data);
  }

  private static Type findSelfType(MethodImplementationNode node) {
    Type selfType = null;
    TypeNameDeclaration methodType = node.getTypeDeclaration();
    if (methodType != null) {
      selfType = methodType.getType();
      if (selfType.isHelper()) {
        selfType = ((HelperType) selfType).extendedType();
      }
    }
    return selfType;
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
    return handleVarDeclaration(node, node.getTypeNode(), data);
  }

  @Override
  public Data visit(FieldDeclarationNode node, Data data) {
    return handleVarDeclaration(node, node.getTypeNode(), data);
  }

  private Data handleVarDeclaration(DelphiNode declarationNode, TypeNode typeNode, Data data) {
    if (typeNode instanceof RecordTypeNode || typeNode instanceof EnumTypeNode) {
      createAnonymousTypeScope(typeNode, data);
    } else {
      resolve(typeNode);
    }

    for (int i = 0; i < declarationNode.jjtGetNumChildren(); ++i) {
      DelphiNode child = (DelphiNode) declarationNode.jjtGetChild(i);
      if (!(child instanceof TypeNode)) {
        child.accept(this, data);
      }
    }

    return data;
  }

  @Override
  public Data visit(ConstDeclarationNode node, Data data) {
    TypeNode typeNode = node.getTypeNode();
    if (typeNode != null) {
      resolve(node.getTypeNode());
    }

    // Visit the expression before we visit the name declaration node and create a name declaration
    // We need the type before we can create the name declaration,
    // and we have to infer the type from the expression for true constants.
    node.getExpression().accept(this, data);
    node.getNameDeclarationNode().accept(this, data);

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
      case PARAMETER:
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
    resolve(node);
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

        if (classOf.getType().isUnresolved()) {
          resolve(classOf);
          ((ClassReferenceType) classReference.getType()).setClassType(classOf.getType());
        }
      } else if (typeDeclaration.isPointer()) {
        var pointer = (PointerTypeNode) typeDeclaration.getTypeNode();
        TypeNode dereferenced = pointer.getDereferencedTypeNode();

        if (dereferenced.getType().isUnresolved()) {
          resolve(dereferenced);
          ((PointerType) pointer.getType()).setDereferencedType(dereferenced.getType());
        }
      }
    }

    return data;
  }
}
