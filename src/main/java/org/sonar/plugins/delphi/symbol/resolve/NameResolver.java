package org.sonar.plugins.delphi.symbol.resolve;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getFirst;
import static com.google.common.collect.Iterables.getLast;
import static java.util.function.Predicate.not;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.INCOMPATIBLE_TYPES;
import static org.sonar.plugins.delphi.symbol.scope.UnknownScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiClassReferenceType.classOf;
import static org.sonar.plugins.delphi.type.DelphiFileType.untypedFile;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.ANSICHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.UNICODESTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.WIDECHAR;

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
import org.sonar.plugins.delphi.antlr.ast.node.LiteralNode;
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
import org.sonar.plugins.delphi.antlr.ast.node.RecordExpressionItemNode;
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
import org.sonar.plugins.delphi.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.FileScope;
import org.sonar.plugins.delphi.symbol.scope.MethodScope;
import org.sonar.plugins.delphi.symbol.scope.TypeScope;
import org.sonar.plugins.delphi.symbol.scope.UnknownScope;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ClassReferenceType;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.HelperType;
import org.sonar.plugins.delphi.type.Type.PointerType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.ScopedType;
import org.sonar.plugins.delphi.type.Type.StructType;
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
    if (declarations.size() + resolvedDeclarations.size() < names.size()) {
      return unknownType();
    }

    if (!declarations.isEmpty()) {
      NameDeclaration declaration = getLast(declarations);
      if (declaration instanceof TypedDeclaration) {
        Type result = findTypeForTypedDeclaration((TypedDeclaration) declaration);
        if (declaration instanceof TypeNameDeclaration) {
          result = classOf(result);
        }
        return result;
      }
      return unknownType();
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
      if (declaration instanceof TypedDeclaration) {
        currentType = findTypeForTypedDeclaration((TypedDeclaration) declaration);
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

  private Type findTypeForTypedDeclaration(TypedDeclaration declaration) {
    Type result;
    if (isConstructor(declaration)) {
      result = currentType;
      if (result.isClassReference()) {
        result = ((ClassReferenceType) result).classType();
      }
    } else {
      return declaration.getType();
    }

    return result;
  }

  private boolean isConstructor(NameDeclaration declaration) {
    return declaration instanceof MethodNameDeclaration
        && ((MethodNameDeclaration) declaration).getMethodKind() == MethodKind.CONSTRUCTOR;
  }

  void readPrimaryExpression(PrimaryExpressionNode node) {
    if (handleInheritedExpression(node)) {
      return;
    }

    for (int i = 0; i < node.jjtGetNumChildren(); i++) {
      if (!readPrimaryExpressionPart(node.jjtGetChild(i))) {
        break;
      }
    }
  }

  /**
   * Reads part of a primary expression
   *
   * @param node part of a primary expression
   * @return false if a name resolution failure occurs
   */
  private boolean readPrimaryExpressionPart(Node node) {
    if (node instanceof NameReferenceNode) {
      readNameReference((NameReferenceNode) node);
    } else if (node instanceof ArgumentListNode) {
      handleArgumentList((ArgumentListNode) node);
    } else if (node instanceof ArrayAccessorNode) {
      handleArrayAccessor(((ArrayAccessorNode) node));
    } else if (node instanceof ParenthesizedExpressionNode) {
      handleParenthesizedExpression((ParenthesizedExpressionNode) node);
    } else if (node instanceof LiteralNode) {
      currentType = ((LiteralNode) node).getType();
    } else {
      handlePrimaryExpressionToken(node);
    }

    return names.size() == resolvedDeclarations.size() + Math.min(1, declarations.size());
  }

  private void handlePrimaryExpressionToken(Node node) {
    switch (node.jjtGetId()) {
      case DelphiLexer.POINTER:
        addResolvedDeclaration();
        break;

      case DelphiLexer.STRING:
        currentType = classOf(UNICODESTRING.type);
        break;

      case DelphiLexer.FILE:
        currentType = classOf(untypedFile());
        break;

      default:
        // Do nothing
    }
  }

  private boolean handleInheritedExpression(PrimaryExpressionNode node) {
    if (!node.isInheritedCall()) {
      return false;
    }

    moveToInheritedScope(node);

    if (node.isBareInherited()) {
      MethodImplementationNode method = node.getFirstParentOfType(MethodImplementationNode.class);
      DelphiNode inheritedNode = (DelphiNode) node.jjtGetChild(0);

      DelphiNameOccurrence occurrence =
          new DelphiNameOccurrence(inheritedNode, method.simpleName());
      occurrence.setIsExplicitInvocation(true);
      addName(occurrence);
      searchForDeclaration(occurrence);
      disambiguateIsCallable();
      disambiguateVisibility();
      disambiguateParameters(method.getParameterTypes());
      addResolvedDeclaration();
    } else {
      NameReferenceNode methodName = (NameReferenceNode) node.jjtGetChild(1);
      DelphiNameOccurrence occurrence = new DelphiNameOccurrence(methodName.getIdentifier());
      addName(occurrence);

      declarations = currentScope.findDeclaration(occurrence);

      if (declarations.isEmpty() && currentScope instanceof TypeScope) {
        currentScope = ((TypeScope) currentScope).getSuperTypeScope();
        searchForDeclaration(occurrence);
      }

      disambiguateIsCallable();
      disambiguateVisibility();

      NameReferenceNode nextName = methodName.nextName();
      if (!declarations.isEmpty() && nextName != null) {
        disambiguateImplicitEmptyArgumentList();
        addResolvedDeclaration();
        readNameReference(nextName);
      }
    }

    int nextChild = (node.isBareInherited() ? 1 : 2);

    for (int i = nextChild; i < node.jjtGetNumChildren(); ++i) {
      if (!readPrimaryExpressionPart(node.jjtGetChild(i))) {
        break;
      }
    }

    return true;
  }

  private void moveToInheritedScope(PrimaryExpressionNode node) {
    MethodImplementationNode method = node.getFirstParentOfType(MethodImplementationNode.class);
    checkNotNull(method);

    currentScope = node.getScope();

    TypeNameDeclaration typeDeclaration = method.getTypeDeclaration();
    if (typeDeclaration != null) {
      Type type = typeDeclaration.getType();
      Type newType = type.superType();

      // Rules for inherited statements in helper methods are a bit unintuitive.
      // Inheritance in helpers doesn't work in the same way as classes. It's more like aggregation.
      //
      // For starters, we ignore any helper ancestors and go straight into the extended type.
      // If this is a bare inherited call with no specified method signature, then we even skip the
      // extended type and move into its supertype.
      // See: https://wiki.freepascal.org/Helper_types#Inherited_with_function_name
      if (type.isHelper()) {
        newType = ((HelperType) type).extendedType();
        if (node.isBareInherited()) {
          newType = newType.superType();
        }
      }

      if (newType instanceof ScopedType) {
        currentScope = ((ScopedType) newType).typeScope();
      } else {
        currentScope = unknownScope();
      }
    }
  }

  private void readMethodNameInterfaceReference(NameReferenceNode node) {
    currentScope = node.getScope();

    for (NameReferenceNode reference : node.flatten()) {
      IdentifierNode identifier = reference.getIdentifier();
      DelphiNameOccurrence occurrence = new DelphiNameOccurrence(identifier);

      addName(occurrence);

      // Method name interface references should be resolved quite literally.
      // If we allow it to go through the more general name resolution steps, we could end up
      // inside a class helper or something.
      declarations = currentScope.findDeclaration(occurrence);
      if (declarations.isEmpty()) {
        break;
      }

      if (reference.nextName() != null) {
        addResolvedDeclaration();
      }

      reference.setNameOccurrence(occurrence);
    }
  }

  private void readNameReference(NameReferenceNode node) {
    boolean couldBeUnitNameReference =
        currentScope == null
            || (!(currentScope instanceof UnknownScope) && currentScope.equals(node.getScope()));

    for (NameReferenceNode reference : node.flatten()) {
      if (isExplicitArrayConstructorInvocation(reference)) {
        return;
      }

      IdentifierNode identifier = reference.getIdentifier();
      DelphiNameOccurrence occurrence = new DelphiNameOccurrence(identifier);
      addName(occurrence);
      searchForDeclaration(occurrence);

      boolean foundDeclaration = !declarations.isEmpty();

      disambiguateIsCallable();
      disambiguateVisibility();

      if (reference.nextName() != null) {
        disambiguateImplicitEmptyArgumentList();
        addResolvedDeclaration();
      }

      if (!foundDeclaration && couldBeUnitNameReference) {
        readPossibleUnitNameReference(node);
        return;
      }

      reference.setNameOccurrence(occurrence);
    }
  }

  private boolean isExplicitArrayConstructorInvocation(NameReferenceNode reference) {
    return getLast(resolvedDeclarations, null) instanceof TypeNameDeclaration
        && currentType.isDynamicArray()
        && reference.getLastName().hasImageEqualTo("Create");
  }

  private void readPossibleUnitNameReference(NameReferenceNode node) {
    NameResolver unitNameResolver = new NameResolver();
    if (unitNameResolver.readUnitNameReference(node)) {
      this.currentType = unknownType();
      this.names.clear();
      this.resolvedDeclarations.clear();

      this.currentScope = unitNameResolver.currentScope;
      this.declarations = unitNameResolver.declarations;
      this.names.addAll(unitNameResolver.names);
      this.resolvedDeclarations.addAll(unitNameResolver.resolvedDeclarations);
    }
  }

  private boolean readUnitNameReference(NameReferenceNode node) {
    FileScope fileScope = node.getScope().getEnclosingScope(FileScope.class);

    List<QualifiedDelphiNameDeclaration> unitDeclarations = new ArrayList<>();
    unitDeclarations.addAll(fileScope.getUnitDeclarations());
    unitDeclarations.addAll(fileScope.getImportDeclarations());
    unitDeclarations.sort(Comparator.comparing(DelphiNameDeclaration::getImage).reversed());

    for (QualifiedDelphiNameDeclaration declaration : unitDeclarations) {
      if (matchReferenceToUnitNameDeclaration(node, declaration)) {
        return true;
      }
    }
    return false;
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

  private void handleArrayAccessor(ArrayAccessorNode accessor) {
    if (handleDefaultArrayProperties(accessor)) {
      return;
    }

    if (declarations.stream().anyMatch(NameResolver::isArrayProperty)) {
      disambiguateArguments(accessor.getExpressions(), true);
      addResolvedDeclaration();
      return;
    }

    addResolvedDeclaration();
    accessor.getExpressions().forEach(NameResolver::resolve);

    if (currentType instanceof CollectionType) {
      currentType = ((CollectionType) currentType).elementType();
    } else if (currentType.isNarrowString()) {
      currentType = ANSICHAR.type;
    } else if (currentType.isWideString()) {
      currentType = WIDECHAR.type;
    }
  }

  private void handleParenthesizedExpression(ParenthesizedExpressionNode parenthesized) {
    resolve(parenthesized);
    currentType = parenthesized.getType();

    ScopedType type = extractScopedType(currentType);
    if (type != null) {
      currentScope = type.typeScope();
    } else {
      currentScope = unknownScope();
    }
  }

  private boolean handleDefaultArrayProperties(ArrayAccessorNode accessor) {
    if (!declarations.isEmpty() && isArrayProperty(getLast(declarations))) {
      // An explicit array property access can be handled by argument disambiguation.
      return false;
    }

    addResolvedDeclaration();

    Type type = currentType;
    if (type.isClassReference()) {
      type = ((ClassReferenceType) type).classType();
    } else if (type.isProcedural()) {
      type = ((ProceduralType) type).returnType();
    }

    if (type.isStruct()) {
      StructType structType = (StructType) type;
      Set<NameDeclaration> defaultArrayProperties = structType.findDefaultArrayProperties();
      if (!defaultArrayProperties.isEmpty()) {
        NameResolver propertyResolver = new NameResolver(this);
        propertyResolver.declarations = defaultArrayProperties;
        propertyResolver.disambiguateArguments(accessor.getExpressions(), true);

        NameDeclaration propertyDeclaration = getLast(propertyResolver.resolvedDeclarations);
        if (propertyDeclaration instanceof PropertyNameDeclaration) {
          DelphiNameOccurrence implicitOccurrence = new DelphiNameOccurrence(accessor);
          implicitOccurrence.setNameDeclaration((PropertyNameDeclaration) propertyDeclaration);
          accessor.setImplicitNameOccurrence(implicitOccurrence);
          registerOccurrence(implicitOccurrence);
        }

        currentScope = propertyResolver.currentScope;
        currentType = propertyResolver.currentType;

        return true;
      }
    }

    return false;
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

  private void handleArgumentList(ArgumentListNode node) {
    Node previous = node.jjtGetParent().jjtGetChild(node.jjtGetChildIndex() - 1);
    if (previous instanceof NameReferenceNode
        && isExplicitArrayConstructorInvocation(((NameReferenceNode) previous))) {
      return;
    }
    disambiguateArguments(node.getArguments(), true);
  }

  private boolean handleHardTypeCast(List<ExpressionNode> argumentExpressions) {
    if (declarations.size() != 1 || argumentExpressions.size() != 1) {
      return false;
    }

    if (!(getLast(declarations) instanceof TypeNameDeclaration)) {
      return false;
    }

    addResolvedDeclaration();
    resolve(argumentExpressions.get(0));
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
      if (!(declaration instanceof VariableNameDeclaration
          || declaration instanceof PropertyNameDeclaration)) {
        return false;
      }

      Type variableType = ((Typed) getLast(declarations)).getType();
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

    if (!names.isEmpty()) {
      getLast(names).setIsExplicitInvocation(explicit);
    }

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
      getLast(names).setIsMethodReference();
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

  private void disambiguateArity(int parameterCount) {
    declarations.removeIf(
        declaration -> {
          Invocable invocable = (Invocable) declaration;
          return invocable.getParametersCount() < parameterCount
              || invocable.getRequiredParametersCount() > parameterCount;
        });
  }

  private void disambiguateIsCallable() {
    declarations.removeIf(
        invocable -> invocable instanceof Invocable && !((Invocable) invocable).isCallable());
  }

  private void disambiguateVisibility() {
    declarations.removeIf(not(this::isVisibleDeclaration));
  }

  private boolean isVisibleDeclaration(NameDeclaration declaration) {
    if (declaration instanceof MethodNameDeclaration) {
      DelphiNameOccurrence name = getLast(names);
      MethodScope fromScope = name.getLocation().getScope().getEnclosingScope(MethodScope.class);
      if (fromScope != null) {
        MethodNameDeclaration method = (MethodNameDeclaration) declaration;
        FileScope fromFileScope = fromScope.getEnclosingScope(FileScope.class);
        FileScope toFileScope = method.getScope().getEnclosingScope(FileScope.class);
        boolean isSameUnit = fromFileScope == toFileScope;

        return isMethodVisibleFrom(method, fromScope, isSameUnit);
      }
    }
    return true;
  }

  private boolean isMethodVisibleFrom(
      MethodNameDeclaration method, MethodScope fromMethodScope, boolean isSameUnit) {
    TypeNameDeclaration methodTypeDeclaration = method.getTypeDeclaration();
    if (methodTypeDeclaration == null) {
      return true;
    }

    DelphiScope fromTypeScope = fromMethodScope.getTypeScope();
    if (fromTypeScope instanceof TypeScope) {
      Type fromType = ((TypeScope) fromTypeScope).getType();
      Type toType = methodTypeDeclaration.getType();

      if (fromType.is(toType)) {
        return true;
      } else if (fromType.isSubTypeOf(toType)) {
        return isSuperTypeMethodVisible(method, isSameUnit);
      } else if (isHelperTypeAccessingExtendedType(fromType, toType)) {
        return !method.isPrivate();
      }
    }

    return isOtherTypeMethodVisible(method, isSameUnit);
  }

  private static boolean isHelperTypeAccessingExtendedType(Type fromType, Type toType) {
    if (fromType.isHelper()) {
      Type extendedType = ((HelperType) fromType).extendedType();
      return extendedType.is(toType) || extendedType.isSubTypeOf(toType);
    }
    return false;
  }

  private static boolean isSuperTypeMethodVisible(MethodNameDeclaration method, boolean sameUnit) {
    return !(sameUnit ? method.isStrictPrivate() : method.isPrivate());
  }

  private static boolean isOtherTypeMethodVisible(MethodNameDeclaration method, boolean sameUnit) {
    if (sameUnit) {
      return !method.isStrictPrivate() && !method.isStrictProtected();
    } else {
      return method.isPublic() || method.isPublished();
    }
  }

  private void addName(DelphiNameOccurrence name) {
    names.add(name);
    if (names.size() > 1) {
      DelphiNameOccurrence qualifiedName = names.get(names.size() - 2);
      qualifiedName.setNameWhichThisQualifies(name);
    }
  }

  private void searchForDeclaration(DelphiNameOccurrence occurrence) {
    DelphiScope occurrenceScope = occurrence.getLocation().getScope();
    if (currentScope == null) {
      currentScope = occurrenceScope;
    }

    checkForRecordHelperScope(occurrenceScope);

    Search search = new Search(occurrence);
    search.execute(currentScope);
    declarations = search.getResult();
  }

  /**
   * This will try to find a record helper scope if our current scope isn't a type scope but our
   * current type is valid. Our current type is probably an intrinsic in this case, meaning
   * declarations can only be found if it has a record helper.
   *
   * <p>If a record helper is found, we move into the record helper scope.
   *
   * @param scope The scope to search for the helper type, if we aren't already in a type scope
   */
  private void checkForRecordHelperScope(DelphiScope scope) {
    if (!currentType.isUnknown() && !(currentScope instanceof TypeScope)) {
      HelperType type = scope.getHelperForType(currentType);
      if (type != null) {
        currentScope = type.typeScope();
      }
    }
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
      } else if (type instanceof ClassReferenceType) {
        type = ((ClassReferenceType) type).classType();
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
      registerOccurrence(name);
    }
  }

  private static void registerOccurrence(DelphiNameOccurrence occurrence) {
    occurrence
        .getLocation()
        .getScope()
        .getEnclosingScope(FileScope.class)
        .registerOccurrence(occurrence.getLocation(), occurrence);
  }

  public static void resolve(MethodDeclarationNode method) {
    resolveMethod(method);
  }

  public static boolean resolve(MethodImplementationNode method) {
    resolveMethod(method);

    NameResolver resolver = new NameResolver();
    resolver.readMethodNameInterfaceReference(method.getNameReferenceNode());
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
    type.clearCachedType();

    List<Node> nodes = new ArrayList<>();
    nodes.add(type);
    nodes.addAll(type.findDescendantsOfType(TypeNode.class));
    nodes.addAll(type.findChildrenOfType(PrimaryExpressionNode.class));

    for (Node node : nodes) {
      node.findChildrenOfType(NameReferenceNode.class).forEach(NameResolver::resolve);
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

    if (handleMethodReference(expression, resolver)) {
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

  private static boolean handleMethodReference(
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
    } else if (parent instanceof RecordExpressionItemNode) {
      resolver.addToSymbolTable();
      return true;
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

  private static void resolve(ExpressionNode expression) {
    if (expression instanceof PrimaryExpressionNode) {
      resolve((PrimaryExpressionNode) expression);
      return;
    }
    resolveSubExpressions(expression);
  }

  private static void resolveSubExpressions(ExpressionNode expression) {
    if (expression instanceof AnonymousMethodNode) {
      return;
    }

    for (var descendant : expression.findDescendantsOfType(PrimaryExpressionNode.class)) {
      resolve(descendant);
    }
  }

  @Nullable
  public static DelphiScope findTypeScope(MethodImplementationNode method) {
    NameResolver resolver = new NameResolver();
    resolver.readNameReference(method.getNameReferenceNode());

    if (resolver.resolvedDeclarations.size() == resolver.names.size() - 1) {
      NameDeclaration declaration = getLast(resolver.resolvedDeclarations, null);
      if (declaration instanceof TypeNameDeclaration) {
        Type type = ((TypeNameDeclaration) declaration).getType();
        if (type instanceof ScopedType) {
          return ((ScopedType) type).typeScope();
        }
      }
    }

    return null;
  }

  public static Type resolvePropertyType(PropertyNode property) {
    Type type = property.getType();
    DelphiScope scope = property.getScope();

    var imaginaryLocation = SymbolicNode.imaginary(property.getPropertyName().getImage(), scope);
    var occurrence = new DelphiNameOccurrence(imaginaryLocation);

    while (type.isUnknown() && scope instanceof TypeScope) {
      scope = ((TypeScope) scope).getSuperTypeScope();
      if (scope != null) {
        NameDeclaration declaration = getFirst(scope.findDeclaration(occurrence), null);
        if (declaration instanceof PropertyNameDeclaration) {
          type = ((PropertyNameDeclaration) declaration).getType();
        }
      }
    }

    return type;
  }
}
