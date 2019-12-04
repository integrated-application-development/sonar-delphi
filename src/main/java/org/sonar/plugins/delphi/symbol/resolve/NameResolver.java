package org.sonar.plugins.delphi.symbol.resolve;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getLast;
import static java.util.function.Predicate.not;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.INCOMPATIBLE_TYPES;
import static org.sonar.plugins.delphi.symbol.scope.UnknownScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType.ANSICHAR;
import static org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType.WIDECHAR;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.apache.commons.lang3.StringUtils;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.AnonymousMethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.ArrayAccessorNode;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterListNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode;
import org.sonar.plugins.delphi.antlr.ast.node.IdentifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodHeadingNode.MethodKind;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodParametersNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodResolutionClauseNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodReturnTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.ParenthesizedExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyReadSpecifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyWriteSpecifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.Search;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.ParameterDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.QualifiedDelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.FileScope;
import org.sonar.plugins.delphi.symbol.scope.TypeScope;
import org.sonar.plugins.delphi.symbol.scope.UnknownScope;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.PointerType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.ScopedType;
import org.sonar.plugins.delphi.type.Typed;

public class NameResolver {

  private static class DisambiguationException extends RuntimeException {
    DisambiguationException(Set<NameDeclaration> declarations, DelphiNameOccurrence occurrence) {
      super(
          "Ambiguous declarations could not be resolved\n[Occurrence]  "
              + occurrence
              + "\n[Declaration] "
              + StringUtils.join(declarations, "\n[Declaration] "));
    }
  }

  private Set<NameDeclaration> declarations = new HashSet<>();
  private DelphiScope currentScope;
  private Type currentType = unknownType();

  private final List<DelphiNameOccurrence> names = new ArrayList<>();
  private final List<NameDeclaration> resolvedDeclarations = new ArrayList<>();

  NameResolver() {
    // Default constructor
  }

  NameResolver(NameResolver resolver) {
    declarations.addAll(resolver.declarations);
    currentScope = resolver.currentScope;
    currentType = resolver.currentType;
    names.addAll(resolver.names);
    resolvedDeclarations.addAll(resolver.resolvedDeclarations);
  }

  boolean isExplicitInvocation() {
    return !names.isEmpty() && getLast(names).isExplicitInvocation();
  }

  Type getApproximateType() {
    if (!declarations.isEmpty()) {
      NameDeclaration declaration = getLast(declarations);
      if (isConstructor(declaration)) {
        return currentType;
      } else if (declaration instanceof Typed) {
        return ((Typed) declaration).getType();
      } else {
        return DelphiType.unknownType();
      }
    }
    return currentType;
  }

  @Nullable
  private NameDeclaration addResolvedDeclaration() {
    NameDeclaration resolved = null;

    if (!declarations.isEmpty()) {
      resolved = getLast(declarations);
      checkAmbiguity();
      updateScopeAndType();
      resolvedDeclarations.addAll(declarations);
      declarations.clear();
    }

    return resolved;
  }

  void checkAmbiguity() {
    if (declarations.size() > 1) {
      throw new DisambiguationException(declarations, getLast(names));
    }
  }

  private void updateScopeAndType() {
    DelphiScope newScope = unknownScope();
    if (declarations.size() == 1) {
      NameDeclaration declaration = getLast(declarations);
      if (isConstructor(declaration)) {
        return;
      } else if (declaration instanceof Typed) {
        Typed typed = (Typed) declaration;
        currentType = typed.getType();
        ScopedType scopedType = extractScopedType(currentType);
        if (scopedType != null) {
          newScope = scopedType.typeScope();
        }
      } else if (declaration instanceof UnitImportNameDeclaration) {
        FileScope unitScope = ((UnitImportNameDeclaration) declaration).getUnitScope();
        if (unitScope != null) {
          newScope = unitScope;
        }
      } else if (declaration instanceof UnitNameDeclaration) {
        newScope = ((UnitNameDeclaration) declaration).getUnitScope();
      }
    }
    currentScope = newScope;
  }

  private boolean isConstructor(NameDeclaration declaration) {
    return declaration instanceof MethodNameDeclaration
        && ((MethodNameDeclaration) declaration).getMethodKind() == MethodKind.CONSTRUCTOR;
  }

  private void moveToInheritedScope(PrimaryExpressionNode node) {
    MethodImplementationNode method = node.getFirstParentOfType(MethodImplementationNode.class);
    checkNotNull(method);

    TypeNameDeclaration typeDeclaration = method.getTypeDeclaration();
    if (typeDeclaration != null) {
      Type superType = method.getTypeDeclaration().getType().superType();
      if (superType instanceof ScopedType) {
        currentScope = ((ScopedType) superType).typeScope();
      } else {
        currentScope = unknownScope();
      }
    }
  }

  void readPrimaryExpression(PrimaryExpressionNode node) {
    if (handleBareInherited(node)) {
      return;
    }

    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      DelphiNode child = (DelphiNode) node.jjtGetChild(i);
      if (child instanceof NameReferenceNode) {
        readNameReference((NameReferenceNode) child);
      } else if (child instanceof ArgumentListNode) {
        disambiguateArguments(((ArgumentListNode) child).getArguments());
      } else if (child instanceof ArrayAccessorNode) {
        handleArrayAccessor(((ArrayAccessorNode) child).getExpressions());
      } else if (child instanceof ParenthesizedExpressionNode) {
        handleParenthesizedExpression((ParenthesizedExpressionNode) child);
      } else if (child.jjtGetId() == DelphiLexer.POINTER) {
        addResolvedDeclaration();
      } else if (child.jjtGetId() == DelphiLexer.INHERITED) {
        moveToInheritedScope(node);
      }

      if (names.size() > resolvedDeclarations.size() + Math.min(1, declarations.size())) {
        break;
      }
    }
  }

  private boolean handleBareInherited(PrimaryExpressionNode node) {
    if (!node.isBareInherited()) {
      return false;
    }

    MethodImplementationNode method = node.getFirstParentOfType(MethodImplementationNode.class);
    DelphiNode inheritedNode = (DelphiNode) node.jjtGetChild(0);

    DelphiNameOccurrence occurrence = new DelphiNameOccurrence(inheritedNode, method.simpleName());
    occurrence.setIsExplicitInvocation(true);
    addName(occurrence);

    moveToInheritedScope(node);
    searchForDeclaration(occurrence);
    disambiguateParameters(method.getParameterTypes());
    addResolvedDeclaration();

    if (currentType instanceof ProceduralType) {
      currentType = ((ProceduralType) currentType).returnType();
    }

    return true;
  }

  private void readNameReference(NameReferenceNode node) {
    for (NameReferenceNode reference : node.flatten()) {
      IdentifierNode identifier = reference.getIdentifier();
      DelphiNameOccurrence occurrence = new DelphiNameOccurrence(identifier);

      addName(occurrence);
      searchForDeclaration(occurrence);

      boolean foundDeclaration = !declarations.isEmpty();

      if (reference.nextName() != null) {
        disambiguateImplicitEmptyArgumentList();
        addResolvedDeclaration();
      }

      if (!foundDeclaration) {
        readPossibleUnitNameReference(node);
        return;
      }

      reference.setNameOccurrence(occurrence);
    }
  }

  private void readPossibleUnitNameReference(NameReferenceNode node) {
    if (names.size() > 1) {
      // Drat. Looks like we were in the middle of a name already. This can't be a unit name.
      return;
    }

    Preconditions.checkNotNull(currentScope);
    if (currentScope instanceof UnknownScope) {
      return;
    }

    names.clear();

    FileScope fileScope = currentScope.getEnclosingScope(FileScope.class);
    List<QualifiedDelphiNameDeclaration> unitDeclarations = new ArrayList<>();
    unitDeclarations.addAll(fileScope.getUnitDeclarations().keySet());
    unitDeclarations.addAll(fileScope.getImportDeclarations().keySet());
    unitDeclarations.sort(Comparator.comparing(DelphiNameDeclaration::getImage).reversed());

    for (QualifiedDelphiNameDeclaration declaration : unitDeclarations) {
      if (matchReferenceToUnitNameDeclaration(node, declaration)) {
        break;
      }
    }
  }

  private boolean matchReferenceToUnitNameDeclaration(
      NameReferenceNode node, QualifiedDelphiNameDeclaration declaration) {
    List<String> declarationParts = declaration.getQualifiedNameParts();
    List<NameReferenceNode> references = node.flatten();

    if (declarationParts.size() > references.size()) {
      return false;
    }

    for (int i = 0; i < declarationParts.size(); ++i) {
      if (!references.get(i).getIdentifier().hasImageEqualTo(declarationParts.get(i))) {
        return false;
      }
    }

    StringBuilder referenceImage = new StringBuilder();
    for (int i = 0; i < declarationParts.size(); ++i) {
      if (i > 0) {
        referenceImage.append('.');
      }
      referenceImage.append(references.get(i).getIdentifier().getImage());
    }

    SymbolicNode symbolicNode =
        SymbolicNode.fromRange(
            referenceImage.toString(),
            node,
            references.get(declarationParts.size() - 1).getIdentifier());

    DelphiNameOccurrence occurrence = new DelphiNameOccurrence(symbolicNode);
    node.setNameOccurrence(occurrence);
    addName(occurrence);
    declarations.add(declaration);
    addResolvedDeclaration();

    if (references.size() > declarationParts.size()) {
      readNameReference(references.get(declarationParts.size()));
    }

    return true;
  }

  private void handleArrayAccessor(List<ExpressionNode> arguments) {
    if (!handleDefaultArrayProperties(arguments)
        && declarations.stream().anyMatch(NameResolver::isArrayProperty)) {
      disambiguateArguments(arguments);
      addResolvedDeclaration();
      return;
    }

    addResolvedDeclaration();
    if (currentType instanceof CollectionType) {
      currentType = ((CollectionType) currentType).elementType();
    } else if (currentType.isNarrowString()) {
      currentType = ANSICHAR.type;
    } else if (currentType.isWideString()) {
      currentType = WIDECHAR.type;
    }
  }

  private void handleParenthesizedExpression(ParenthesizedExpressionNode parenthesized) {
    ExpressionNode expression = parenthesized.getExpression();
    resolveSubExpressions(expression);

    currentType = expression.getType();

    ScopedType type = extractScopedType(currentType);
    if (type != null) {
      currentScope = type.typeScope();
    } else {
      currentScope = unknownScope();
    }
  }

  private boolean handleDefaultArrayProperties(List<ExpressionNode> arguments) {
    if (!declarations.isEmpty() && getLast(declarations) instanceof PropertyNameDeclaration) {
      // An explicit property access can be handled by argument disambiguation.
      return false;
    }

    addResolvedDeclaration();

    if (currentScope instanceof TypeScope) {
      Set<NameDeclaration> defaultArrayProperties = findDefaultArrayProperties(currentScope);
      if (!defaultArrayProperties.isEmpty()) {
        NameResolver propertyResolver = new NameResolver(this);
        propertyResolver.declarations = defaultArrayProperties;
        propertyResolver.disambiguateArguments(arguments);
        currentScope = propertyResolver.currentScope;
        currentType = propertyResolver.currentType;
        return true;
      }
    }

    return false;
  }

  private static Set<NameDeclaration> findDefaultArrayProperties(DelphiScope scope) {
    Set<NameDeclaration> result = new HashSet<>();
    findDefaultArrayProperties(scope, result);
    return result;
  }

  private static void findDefaultArrayProperties(DelphiScope scope, Set<NameDeclaration> result) {
    scope.getPropertyDeclarations().keySet().stream()
        .filter(PropertyNameDeclaration::isArrayProperty)
        .filter(PropertyNameDeclaration::isDefaultProperty)
        .forEach(result::add);

    if (scope.getParent() != null) {
      findDefaultArrayProperties(scope.getParent(), result);
    }
  }

  private static boolean isArrayProperty(NameDeclaration declaration) {
    return declaration instanceof PropertyNameDeclaration
        && ((PropertyNameDeclaration) declaration).isArrayProperty();
  }

  void disambiguateImplicitEmptyArgumentList() {
    if (declarations.stream().noneMatch(MethodNameDeclaration.class::isInstance)) {
      return;
    }
    disambiguateArguments(Collections.emptyList(), false);
  }

  private void disambiguateArguments(List<ExpressionNode> argumentExpressions) {
    disambiguateArguments(argumentExpressions, true);
  }

  private boolean handleHardTypeCast(List<ExpressionNode> argumentExpressions) {
    if (declarations.size() != 1 || argumentExpressions.size() != 1) {
      return false;
    }

    if (!(getLast(declarations) instanceof TypeNameDeclaration)) {
      return false;
    }

    addResolvedDeclaration();
    resolveSubExpressions(argumentExpressions.get(0));
    return true;
  }

  private boolean handleProcVarInvocation(List<ExpressionNode> argumentExpressions) {
    if (declarations.size() > 1) {
      return false;
    }

    ProceduralType proceduralType;

    if (declarations.isEmpty()) {
      if (!currentType.isProcedural()) {
        return false;
      }
      proceduralType = (ProceduralType) currentType;
    } else {
      NameDeclaration declaration = getLast(declarations);
      if (!(declaration instanceof VariableNameDeclaration)) {
        return false;
      }

      Type variableType = ((VariableNameDeclaration) getLast(declarations)).getType();
      if (!variableType.isProcedural()) {
        return false;
      }

      proceduralType = (ProceduralType) variableType;
      addResolvedDeclaration();
    }

    List<Type> parameterTypes = proceduralType.parameterTypes();
    int count = Math.min(argumentExpressions.size(), parameterTypes.size());

    for (int i = 0; i < count; ++i) {
      ExpressionNode argument = argumentExpressions.get(i);
      resolveSubExpressions(argument);
      InvocationArgument invocationArgument = new InvocationArgument(argument);
      invocationArgument.resolve(parameterTypes.get(i));
    }

    currentType = proceduralType.returnType();
    return true;
  }

  private void disambiguateArguments(List<ExpressionNode> argumentExpressions, boolean explicit) {
    if (handleHardTypeCast(argumentExpressions) || handleProcVarInvocation(argumentExpressions)) {
      return;
    }

    if (declarations.isEmpty()) {
      return;
    }

    disambiguateInvocable();
    disambiguateIsCallable();
    disambiguateArity(argumentExpressions.size());

    argumentExpressions.forEach(NameResolver::resolveSubExpressions);

    InvocationResolver resolver = new InvocationResolver();
    argumentExpressions.stream().map(InvocationArgument::new).forEach(resolver::addArgument);
    declarations.stream()
        .map(Invocable.class::cast)
        .map(InvocationCandidate::new)
        .forEach(resolver::addCandidate);

    resolver.processCandidates();
    Set<InvocationCandidate> bestCandidate = resolver.chooseBest();

    declarations =
        bestCandidate.stream()
            .map(InvocationCandidate::getData)
            .map(NameDeclaration.class::cast)
            .collect(Collectors.toSet());

    DelphiNameOccurrence name = getLast(names);
    name.setIsExplicitInvocation(explicit);

    NameDeclaration resolved = addResolvedDeclaration();

    if (resolved == null) {
      // we can't find it, so just give up
      return;
    }

    Invocable invocable = ((Invocable) resolved);

    for (int i = 0; i < resolver.getArguments().size(); ++i) {
      InvocationArgument argument = resolver.getArguments().get(i);
      ParameterDeclaration parameter = invocable.getParameter(i);
      argument.resolve(parameter.getType());
    }

    if (!isConstructor(resolved)) {
      currentType = invocable.getReturnType();
    }
  }

  void disambiguateMethodReference(ProceduralType procedure) {
    disambiguateInvocable();
    disambiguateIsCallable();

    EqualityType bestEquality = INCOMPATIBLE_TYPES;
    NameDeclaration bestDeclaration = null;

    for (NameDeclaration declaration : declarations) {
      if (declaration instanceof Typed) {
        EqualityType equality = TypeComparer.compare(((Typed) declaration).getType(), procedure);
        if (equality != INCOMPATIBLE_TYPES && equality.ordinal() >= bestEquality.ordinal()) {
          bestEquality = equality;
          bestDeclaration = declaration;
        }
      }
    }

    declarations.clear();

    if (bestDeclaration != null) {
      declarations.add(bestDeclaration);
    }

    DelphiNameOccurrence name = getLast(names);
    name.setIsMethodReference();
  }

  private void disambiguateParameters(List<Type> parameterTypes) {
    disambiguateInvocable();
    disambiguateArity(parameterTypes.size());

    declarations.removeIf(
        declaration -> {
          List<ParameterDeclaration> parameters = ((Invocable) declaration).getParameters();
          if (parameterTypes.size() != parameters.size()) {
            return true;
          }

          for (int i = 0; i < parameters.size(); ++i) {
            if (!parameters.get(i).getType().is(parameterTypes.get(i))) {
              return true;
            }
          }

          return false;
        });
  }

  private void disambiguateIsClassInvocable(boolean isClassInvocable) {
    declarations.removeIf(
        declaration -> ((Invocable) declaration).isClassInvocable() != isClassInvocable);
  }

  private void disambiguateQualifiedMethodName(final String fullyQualifiedName) {
    declarations.removeIf(
        declaration ->
            !(declaration instanceof MethodNameDeclaration)
                || !((MethodNameDeclaration) declaration)
                    .fullyQualifiedName()
                    .equalsIgnoreCase(fullyQualifiedName));
  }

  private void disambiguateInvocable() {
    declarations.removeIf(not(Invocable.class::isInstance));
  }

  private void disambiguateIsCallable() {
    declarations.removeIf(invocable -> !((Invocable) invocable).isCallable());
  }

  private void disambiguateArity(int parameterCount) {
    declarations.removeIf(
        declaration -> {
          Invocable invocable = (Invocable) declaration;
          return invocable.getParametersCount() < parameterCount
              || invocable.getRequiredParametersCount() > parameterCount;
        });
  }

  private void addName(DelphiNameOccurrence name) {
    names.add(name);
    if (names.size() > 1) {
      DelphiNameOccurrence qualifiedName = names.get(names.size() - 2);
      qualifiedName.setNameWhichThisQualifies(name);
    }
  }

  private void searchForDeclaration(DelphiNameOccurrence occurrence) {
    Search search = new Search(occurrence);

    if (currentScope == null) {
      currentScope = occurrence.getLocation().getScope();
    }

    search.execute(currentScope);
    declarations = search.getResult();
  }

  @Nullable
  private static ScopedType extractScopedType(Type type) {
    while (!(type instanceof ScopedType)) {
      if (type instanceof ProceduralType) {
        type = ((ProceduralType) type).returnType();
      } else if (type instanceof CollectionType) {
        type = ((CollectionType) type).elementType();
      } else if (type instanceof PointerType) {
        type = ((PointerType) type).dereferencedType();
      } else {
        break;
      }
    }

    return (type instanceof ScopedType) ? (ScopedType) type : null;
  }

  void addToSymbolTable() {
    addResolvedDeclaration();

    for (int i = 0; i < resolvedDeclarations.size(); ++i) {
      DelphiNameOccurrence name = names.get(i);
      DelphiNameDeclaration declaration = (DelphiNameDeclaration) resolvedDeclarations.get(i);
      name.setNameDeclaration(declaration);

      declaration.getScope().addNameOccurrence(name);
      name.getLocation()
          .getScope()
          .getEnclosingScope(FileScope.class)
          .registerOccurrence(name.getLocation(), name);
    }
  }

  public static void resolve(MethodDeclarationNode method) {
    resolveMethod(method);
  }

  public static boolean resolve(MethodImplementationNode method) {
    resolveMethod(method);

    NameResolver resolver = new NameResolver();
    resolver.readNameReference(method.getNameReferenceNode());
    resolver.disambiguateParameters(method.getParameterTypes());
    resolver.disambiguateIsClassInvocable(method.isClassMethod());
    resolver.disambiguateQualifiedMethodName(method.fullyQualifiedName());

    if (!resolver.declarations.isEmpty()) {
      resolver.addToSymbolTable();
      return true;
    }

    return false;
  }

  public static void resolve(TypeDeclarationNode typeDeclaration) {
    typeDeclaration.getTypeNode().getParentTypeNodes().forEach(NameResolver::resolve);
    resolve(typeDeclaration.getTypeNode());
  }

  public static void resolve(TypeNode type) {
    List<TypeNode> types = new ArrayList<>();
    types.add(type);
    types.addAll(type.findDescendantsOfType(TypeNode.class));
    for (TypeNode typeNode : types) {
      typeNode.findChildrenOfType(NameReferenceNode.class).forEach(NameResolver::resolve);
    }
  }

  public static void resolve(NameReferenceNode reference) {
    NameResolver resolver = new NameResolver();
    resolver.readNameReference(reference);
    resolver.addToSymbolTable();
  }

  public static void resolve(PrimaryExpressionNode expression) {
    NameResolver resolver = new NameResolver();
    resolver.readPrimaryExpression(expression);

    if (resolvePotentialMethodReference(expression, resolver)) {
      return;
    }

    if (!resolver.isExplicitInvocation()) {
      resolver.disambiguateImplicitEmptyArgumentList();
    }

    resolver.addToSymbolTable();
  }

  public static void resolve(MethodResolutionClauseNode resolutionClause) {
    NameResolver interfaceMethodResolver = new NameResolver();
    interfaceMethodResolver.readNameReference(resolutionClause.getInterfaceMethodNameNode());
    List<MethodNameDeclaration> interfaceMethods =
        interfaceMethodResolver.declarations.stream()
            .filter(MethodNameDeclaration.class::isInstance)
            .map(MethodNameDeclaration.class::cast)
            .filter(method -> method.getTypeDeclaration() != null)
            .sorted(
                (left, right) -> {
                  Type leftType = left.getTypeDeclaration().getType();
                  TypeNameDeclaration typeDecl = right.getTypeDeclaration();
                  Type rightType = (typeDecl == null) ? unknownType() : typeDecl.getType();

                  if (leftType.is(rightType)) {
                    return left.getNode().getBeginLine() - right.getNode().getBeginLine();
                  } else if (leftType.isSubTypeOf(rightType)) {
                    return 1;
                  } else {
                    return -1;
                  }
                })
            .collect(Collectors.toList());

    NameResolver concreteMethodResolver = new NameResolver();
    concreteMethodResolver.readNameReference(resolutionClause.getImplementationMethodNameNode());
    List<MethodNameDeclaration> implementationMethods =
        concreteMethodResolver.declarations.stream()
            .filter(MethodNameDeclaration.class::isInstance)
            .map(MethodNameDeclaration.class::cast)
            .collect(Collectors.toList());

    interfaceMethodResolver.declarations.clear();
    concreteMethodResolver.declarations.clear();

    for (MethodNameDeclaration interfaceCandidate : interfaceMethods) {
      boolean matched = false;

      for (MethodNameDeclaration concreteCandidate : implementationMethods) {
        if (interfaceCandidate.getParameters().equals(concreteCandidate.getParameters())) {
          interfaceMethodResolver.declarations.add(interfaceCandidate);
          concreteMethodResolver.declarations.add(concreteCandidate);
          matched = true;
          break;
        }
      }

      if (matched) {
        break;
      }
    }

    interfaceMethodResolver.addToSymbolTable();
    concreteMethodResolver.addToSymbolTable();
  }

  private static boolean resolvePotentialMethodReference(
      PrimaryExpressionNode expression, NameResolver resolver) {
    Node parent = expression.jjtGetParent();
    if (parent instanceof AssignmentStatementNode) {
      Typed assignee = ((AssignmentStatementNode) parent).getAssignee();
      if (expression == assignee) {
        return false;
      }

      if (assignee.getType().isProcedural()) {
        NameResolver clone = new NameResolver(resolver);
        clone.disambiguateMethodReference((ProceduralType) assignee.getType());
        if (!clone.declarations.isEmpty()) {
          clone.addToSymbolTable();
          return true;
        }
      }
    }
    return false;
  }

  public static void resolve(PropertyNode property) {
    resolve(property.getParameterListNode());

    TypeNode type = property.getTypeNode();
    if (type != null) {
      resolve(type);
    }

    PropertyReadSpecifierNode read = property.getReadSpecifier();
    if (read != null) {
      NameResolver readResolver = new NameResolver();
      readResolver.readPrimaryExpression(read.getExpression());
      readResolver.disambiguateParameters(property.getParameterTypes());
      readResolver.addToSymbolTable();
    }

    PropertyWriteSpecifierNode write = property.getWriteSpecifier();
    if (write != null) {
      List<Type> parameterTypes = new ArrayList<>(property.getParameterTypes());
      parameterTypes.add(property.getType());
      NameResolver writeResolver = new NameResolver();
      writeResolver.readPrimaryExpression(write.getExpression());
      writeResolver.disambiguateParameters(parameterTypes);
      writeResolver.addToSymbolTable();
    }
  }

  private static void resolveMethod(MethodNode method) {
    resolve(method.getMethodHeading().getMethodParametersNode());
    resolve(method.getMethodHeading().getMethodReturnType());
  }

  public static void resolve(@Nullable MethodParametersNode parameters) {
    if (parameters != null) {
      resolve(parameters.getFormalParametersList());
    }
  }

  public static void resolve(@Nullable MethodReturnTypeNode returnType) {
    if (returnType != null) {
      NameResolver.resolve(returnType.getTypeNode());
    }
  }

  private static void resolve(@Nullable FormalParameterListNode parameterList) {
    if (parameterList != null) {
      parameterList
          .findChildrenOfType(FormalParameterNode.class)
          .forEach(
              parameter -> {
                TypeNode type = parameter.getTypeNode();
                if (type != null) {
                  NameResolver.resolve(type);
                }
              });
    }
  }

  private static void resolveSubExpressions(ExpressionNode argument) {
    if (argument instanceof PrimaryExpressionNode) {
      resolve((PrimaryExpressionNode) argument);
      return;
    }

    if (argument instanceof AnonymousMethodNode) {
      return;
    }

    for (var descendant : argument.findDescendantsOfType(PrimaryExpressionNode.class)) {
      resolve(descendant);
    }
  }
}
