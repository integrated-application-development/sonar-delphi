package org.sonar.plugins.delphi.symbol.resolve;

import static com.google.common.collect.Iterables.getLast;
import static java.util.function.Predicate.not;
import static org.sonar.plugins.delphi.symbol.UnknownScope.unknownScope;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.INCOMPATIBLE_TYPES;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import com.google.common.base.Preconditions;
import java.util.ArrayList;
import java.util.Collections;
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
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodParametersNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodReturnTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyReadSpecifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyWriteSpecifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.symbol.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.DelphiScope;
import org.sonar.plugins.delphi.symbol.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.ParameterDeclaration;
import org.sonar.plugins.delphi.symbol.PropertyNameDeclaration;
import org.sonar.plugins.delphi.symbol.Search;
import org.sonar.plugins.delphi.symbol.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.TypeScope;
import org.sonar.plugins.delphi.symbol.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.VariableNameDeclaration;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.PointerType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.ScopedType;
import org.sonar.plugins.delphi.type.Typed;

public class NameResolver {

  public static class DisambiguationException extends RuntimeException {
    DisambiguationException(Set<NameDeclaration> declarations, DelphiNameOccurrence occurrence) {
      super(
          "Ambiguous declarations could not be resolved\n[Occurrence]  "
              + occurrence
              + "\n[Declaration] "
              + StringUtils.join(declarations, "\n[Declaration] "));
    }
  }

  private Set<NameDeclaration> declarations = new HashSet<>();
  private DelphiScope currentScope = unknownScope();
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
      if (declaration instanceof Typed) {
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
      if (declaration instanceof Typed) {
        Typed typed = (Typed) declaration;
        currentType = typed.getType();
        ScopedType scopedType = extractScopedType(currentType);
        if (scopedType != null) {
          newScope = scopedType.typeScope();
        }
      } else if (declaration instanceof UnitNameDeclaration) {
        newScope = ((UnitNameDeclaration) declaration).getUnitScope();
      }
    }
    currentScope = newScope;
  }

  void readPrimaryExpression(PrimaryExpressionNode node) {
    if (node.isBareInherited()) {
      MethodImplementationNode method = node.getFirstParentOfType(MethodImplementationNode.class);
      DelphiNode inheritedNode = (DelphiNode) node.jjtGetChild(0);
      var occurrence = new DelphiNameOccurrence(inheritedNode, method.simpleName());
      occurrence.setIsInherited();
      occurrence.setIsExplicitInvocation(true);
      addName(occurrence);
      searchForDeclaration(occurrence);
      disambiguateParameters(method.getParameterTypes());
      addResolvedDeclaration();
      if (currentType instanceof ProceduralType) {
        currentType = ((ProceduralType) currentType).returnType();
      }
      return;
    }

    boolean inherited = false;
    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      DelphiNode child = (DelphiNode) node.jjtGetChild(i);
      if (child instanceof NameReferenceNode) {
        NameReferenceNode reference = (NameReferenceNode) child;
        readNameReference(reference, inherited);
        inherited = false;
      } else if (child instanceof ArgumentListNode) {
        disambiguateArguments(((ArgumentListNode) child).getArguments());
      } else if (child instanceof ArrayAccessorNode) {
        handleArrayAccessor(((ArrayAccessorNode) child).getExpressions());
      } else if (child.jjtGetId() == DelphiLexer.POINTER) {
        addResolvedDeclaration();
      } else if (child.jjtGetId() == DelphiLexer.INHERITED) {
        inherited = true;
      }

      if (names.size() > resolvedDeclarations.size() + Math.min(1, declarations.size())) {
        break;
      }
    }
  }

  private void readNameReference(NameReferenceNode node) {
    readNameReference(node, false);
  }

  private void readNameReference(NameReferenceNode node, boolean inherited) {
    for (NameReferenceNode reference : node.flatten()) {
      String image = reference.getIdentifier().getImage();
      DelphiNameOccurrence occurrence = new DelphiNameOccurrence(reference, image);

      if (inherited) {
        occurrence.setIsInherited();
        inherited = false;
      }

      addName(occurrence);
      searchForDeclaration(occurrence);

      boolean foundDeclaration = !declarations.isEmpty();

      if (reference.nextName() != null) {
        addResolvedDeclaration();
      }

      if (!foundDeclaration) {
        // we can't find it, so just give up
        // when we decide to do full symbol resolution force this to either find a symbol or
        // throw a SymbolNotFoundException
        return;
      }

      reference.setNameOccurrence(occurrence);
    }
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

  private void disambiguateArguments(List<ExpressionNode> argumentExpressions, boolean explicit) {
    if (declarations.isEmpty()) {
      return;
    }

    if (declarations.size() == 1 && getLast(declarations) instanceof TypeNameDeclaration) {
      // This only looks like an invocation. It's actually a hard type cast.
      addResolvedDeclaration();
      return;
    }

    if (declarations.size() == 1 && getLast(declarations) instanceof VariableNameDeclaration) {
      // Invocation of a procedural variable
      addResolvedDeclaration();
      return;
    }

    disambiguateInvocable();
    disambiguateIsCallable();
    disambiguateArity(argumentExpressions.size());

    argumentExpressions.forEach(NameResolver::resolveArgumentSubExpressions);

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
      // when we decide to do full symbol resolution force this to either find a symbol or
      // throw a SymbolNotFoundException
      return;
    }

    Invocable invocable = ((Invocable) resolved);

    for (int i = 0; i < resolver.getArguments().size(); ++i) {
      InvocationArgument argument = resolver.getArguments().get(i);
      ParameterDeclaration parameter = invocable.getParameter(i);
      argument.resolve(parameter);
    }

    currentType = invocable.getReturnType();
  }

  void disambiguateMethodReference(ProceduralType procedure) {
    disambiguateInvocable();
    disambiguateIsCallable();

    var iterator = declarations.iterator();
    EqualityType bestEquality = INCOMPATIBLE_TYPES;

    while (iterator.hasNext()) {
      NameDeclaration declaration = iterator.next();
      if (declaration instanceof Typed) {
        EqualityType equality = TypeComparer.compare(((Typed) declaration).getType(), procedure);
        if (equality != INCOMPATIBLE_TYPES && equality.ordinal() >= bestEquality.ordinal()) {
          bestEquality = equality;
          continue;
        }
      }
      iterator.remove();
    }
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

    if (resolvedDeclarations.isEmpty()) {
      currentScope = findStartingScope(occurrence);
    }

    search.execute(currentScope);
    declarations = search.getResult();
  }

  private static DelphiScope findStartingScope(DelphiNameOccurrence occurrence) {
    DelphiScope startingScope = occurrence.getLocation().getScope();
    if (occurrence.isInherited()) {
      var method = occurrence.getLocation().getFirstParentOfType(MethodImplementationNode.class);
      Preconditions.checkNotNull(method);

      TypeNameDeclaration typeDeclaration = method.getTypeDeclaration();
      if (typeDeclaration != null) {
        Type superType = method.getTypeDeclaration().getType().superType();
        if (superType instanceof ScopedType) {
          startingScope = ((ScopedType) superType).typeScope();
        } else {
          // Until we do full symbol resolution, this will only work if the supertype is declared in
          // the same file.
          startingScope = unknownScope();
        }
      }
    }

    return startingScope;
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
    }
  }

  public static void resolve(MethodDeclarationNode method) {
    resolveMethod(method);
  }

  public static void resolve(MethodImplementationNode method) {
    resolveMethod(method);

    NameResolver resolver = new NameResolver();
    resolver.readNameReference(method.getMethodName());
    resolver.disambiguateParameters(method.getParameterTypes());
    resolver.disambiguateIsClassInvocable(method.isClassMethod());
    resolver.disambiguateQualifiedMethodName(method.fullyQualifiedName());

    boolean isImplementationScopedMethod =
        method.getMethodName().nextName() == null && resolver.declarations.isEmpty();

    if (isImplementationScopedMethod) {
      MethodNameDeclaration declaration = new MethodNameDeclaration(method);
      method.getMethodHeading().getMethodNameNode().setNameDeclaration(declaration);
      method.getScope().addDeclaration(declaration);
      return;
    }

    resolver.addToSymbolTable();
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

  private static void resolveArgumentSubExpressions(ExpressionNode argument) {
    if (argument instanceof PrimaryExpressionNode || argument instanceof AnonymousMethodNode) {
      return;
    }

    for (var descendant : argument.findDescendantsOfType(PrimaryExpressionNode.class)) {
      resolve(descendant);
    }
  }
}
