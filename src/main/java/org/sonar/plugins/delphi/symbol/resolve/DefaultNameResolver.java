package org.sonar.plugins.delphi.symbol.resolve;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.getLast;
import static java.util.function.Predicate.not;
import static org.sonar.plugins.delphi.symbol.resolve.EqualityType.INCOMPATIBLE_TYPES;
import static org.sonar.plugins.delphi.symbol.scope.DelphiScope.unknownScope;
import static org.sonar.plugins.delphi.type.DelphiClassReferenceType.classOf;
import static org.sonar.plugins.delphi.type.DelphiFileType.untypedFile;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;
import static org.sonar.plugins.delphi.type.DelphiType.voidType;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.ANSICHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.UNICODESTRING;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.WIDECHAR;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.apache.commons.lang3.StringUtils;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.ArrayAccessorNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.GenericArgumentsNode;
import org.sonar.plugins.delphi.antlr.ast.node.IdentifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.LiteralNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodHeadingNode.MethodKind;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.ParenthesizedExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeReferenceNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.Search;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.GenerifiableDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.QualifiedDelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeParameterNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.parameter.Parameter;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.FileScope;
import org.sonar.plugins.delphi.symbol.scope.MethodScope;
import org.sonar.plugins.delphi.symbol.scope.TypeScope;
import org.sonar.plugins.delphi.symbol.scope.UnknownScope;
import org.sonar.plugins.delphi.type.DelphiTypeParameterType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ClassReferenceType;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.HelperType;
import org.sonar.plugins.delphi.type.Type.PointerType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.ScopedType;
import org.sonar.plugins.delphi.type.Type.StructType;
import org.sonar.plugins.delphi.type.Type.TypeParameterType;
import org.sonar.plugins.delphi.type.Typed;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicReturnType;

public class DefaultNameResolver implements NameResolver {
  private final List<DelphiNameOccurrence> names = new ArrayList<>();
  private final List<NameDeclaration> resolvedDeclarations = new ArrayList<>();

  private Set<NameDeclaration> declarations = new HashSet<>();
  private DelphiScope currentScope;
  private Type currentType = unknownType();

  DefaultNameResolver() {
    // Default constructor
  }

  DefaultNameResolver(DefaultNameResolver resolver) {
    declarations.addAll(resolver.declarations);
    currentScope = resolver.currentScope;
    currentType = resolver.currentType;
    names.addAll(resolver.names);
    resolvedDeclarations.addAll(resolver.resolvedDeclarations);
  }

  @Override
  public Type getApproximateType() {
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

  @Override
  public Set<NameDeclaration> getDeclarations() {
    return declarations;
  }

  @Override
  public List<NameDeclaration> getResolvedDeclarations() {
    return resolvedDeclarations;
  }

  @Override
  public NameDeclaration addResolvedDeclaration() {
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

  @Override
  public void checkAmbiguity() {
    if (declarations.size() > 1) {
      throw new DisambiguationException(declarations, getLast(names));
    }
  }

  @Override
  public void updateScopeAndType() {
    DelphiScope newScope = unknownScope();
    if (declarations.size() == 1) {
      NameDeclaration declaration = getLast(declarations);
      if (declaration instanceof TypedDeclaration) {
        currentType = findTypeForTypedDeclaration((TypedDeclaration) declaration);
        ScopedType scopedType = DefaultNameResolver.extractScopedType(currentType);
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

  @Override
  public boolean isExplicitInvocation() {
    return !names.isEmpty() && getLast(names).isExplicitInvocation();
  }

  @Override
  public void addToSymbolTable() {
    addResolvedDeclaration();

    for (int i = 0; i < resolvedDeclarations.size(); ++i) {
      DelphiNameOccurrence name = names.get(i);
      DelphiNameDeclaration declaration = (DelphiNameDeclaration) resolvedDeclarations.get(i);
      name.setNameDeclaration(declaration);

      declaration.getScope().addNameOccurrence(name);
      registerOccurrence(name);
    }
  }

  private Type findTypeForTypedDeclaration(TypedDeclaration declaration) {
    Type result;
    if (isConstructor(declaration)) {
      result = currentType;
      if (result.isClassReference()) {
        result = ((ClassReferenceType) result).classType();
      }
    } else {
      result = declaration.getType();
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

    return !nameResolutionFailed();
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
      if (!declarations.isEmpty()) {
        methodName.setNameOccurrence(occurrence);
        if (nextName != null) {
          disambiguateImplicitEmptyArgumentList();
          addResolvedDeclaration();
          readNameReference(nextName);
        }
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

  void readMethodNameInterfaceReference(NameReferenceNode node) {
    currentScope = node.getScope().getParent();

    for (NameReferenceNode reference : node.flatten()) {
      IdentifierNode identifier = reference.getIdentifier();
      DelphiNameOccurrence occurrence = new DelphiNameOccurrence(identifier);

      addName(occurrence);

      // Method name interface references should be resolved quite literally.
      // If we allow it to go through the more general name resolution steps and scope traversals,
      // we could end up inside a class helper or parent type or something.
      declarations = currentScope.findDeclaration(occurrence);
      declarations.removeIf(declaration -> declaration.getScope() != currentScope);

      GenericArgumentsNode genericArguments = reference.getGenericArguments();
      if (genericArguments != null) {
        List<TypeReferenceNode> typeReferences =
            genericArguments.findChildrenOfType(TypeReferenceNode.class);
        disambiguateExplicitGeneric(typeReferences.size());
        occurrence.setIsGeneric();

        if (reference.nextName() != null) {
          // If we're in the middle of the name, then we assume that we have already properly
          // disambiguated the reference, and resolve the type parameter references back to their
          // declarations on that type reference
          handleTypeParameterReferences(typeReferences);
        } else {
          // If we're on the last name, then it's the method reference.
          // It's impossible for us to know which method we're referring to before parameter
          // resolution occurs.
          // Instead, we create temporary type parameter declarations that will live in the method
          // scope as "forward" declarations. Once parameter resolution has occurred, we can come
          // back and complete the type parameter declaration/type.
          MethodScope methodScope = node.getScope().getEnclosingScope(MethodScope.class);
          handleTypeParameterForwardReferences(methodScope, typeReferences);
        }

        occurrence.setTypeArguments(
            typeReferences.stream()
                .map(TypeReferenceNode::getType)
                .collect(Collectors.toUnmodifiableList()));
      }

      if (!occurrence.isGeneric()) {
        disambiguateNotGeneric();
      }

      String unitName = occurrence.getLocation().getUnitName();
      disambiguateWithinUnit(unitName);

      if (declarations.isEmpty()) {
        break;
      }

      if (reference.nextName() != null) {
        addResolvedDeclaration();
      }

      reference.setNameOccurrence(occurrence);
    }
  }

  private void handleTypeParameterReferences(List<TypeReferenceNode> typeReferences) {
    NameDeclaration currentDeclaration = getLast(declarations, null);
    if (!(currentDeclaration instanceof GenerifiableDeclaration)) {
      return;
    }

    GenerifiableDeclaration generifiable = (GenerifiableDeclaration) currentDeclaration;
    List<TypedDeclaration> typeParameters = generifiable.getTypeParameters();
    if (typeParameters.size() != typeReferences.size()) {
      return;
    }

    for (int i = 0; i < typeParameters.size(); ++i) {
      TypedDeclaration declaration = typeParameters.get(i);
      NameReferenceNode typeReference = typeReferences.get(i).getNameNode();
      DelphiNameOccurrence occurrence = new DelphiNameOccurrence(typeReference);

      occurrence.setNameDeclaration(declaration);
      typeReference.setNameOccurrence(occurrence);

      DefaultNameResolver resolver = new DefaultNameResolver();
      resolver.resolvedDeclarations.add(declaration);
      resolver.names.add(occurrence);
      resolver.addToSymbolTable();
    }
  }

  private void handleTypeParameterForwardReferences(
      MethodScope methodScope, List<TypeReferenceNode> typeReferences) {
    for (TypeReferenceNode typeNode : typeReferences) {
      NameReferenceNode typeReference = typeNode.getNameNode();
      TypeParameterType type = DelphiTypeParameterType.create(typeReference.getImage());
      DelphiNameDeclaration declaration = new TypeParameterNameDeclaration(typeReference, type);
      methodScope.addDeclaration(declaration);

      DelphiNameOccurrence occurrence = new DelphiNameOccurrence(typeReference);
      occurrence.setNameDeclaration(declaration);
      typeReference.setNameOccurrence(occurrence);

      DefaultNameResolver resolver = new DefaultNameResolver();
      resolver.resolvedDeclarations.add(declaration);
      resolver.names.add(occurrence);
      resolver.addToSymbolTable();
    }
  }

  void readNameReference(NameReferenceNode node) {
    boolean couldBeUnitNameReference =
        currentScope == null
            || (!(currentScope instanceof UnknownScope) && currentScope.equals(node.getScope()));

    for (NameReferenceNode reference : node.flatten()) {
      if (isExplicitArrayConstructorInvocation(reference)) {
        return;
      }

      DelphiNameOccurrence occurrence = createNameOccurrence(reference);
      addName(occurrence);
      searchForDeclaration(occurrence);

      if (occurrence.isGeneric()) {
        disambiguateExplicitGeneric(occurrence.getTypeArguments().size());
        specializeDeclarations(occurrence);
      } else {
        disambiguateNotGeneric();
      }

      disambiguateIsCallable();
      disambiguateVisibility();

      if (reference.nextName() != null) {
        disambiguateImplicitEmptyArgumentList();
        addResolvedDeclaration();
      }

      if (nameResolutionFailed()) {
        if (couldBeUnitNameReference) {
          readPossibleUnitNameReference(node);
        }
        return;
      }

      reference.setNameOccurrence(occurrence);
    }
  }

  private static DelphiNameOccurrence createNameOccurrence(NameReferenceNode reference) {
    DelphiNameOccurrence occurrence = new DelphiNameOccurrence(reference.getIdentifier());
    GenericArgumentsNode genericArguments = reference.getGenericArguments();
    if (genericArguments != null) {
      List<TypeNode> typeArgumentNodes = genericArguments.getTypeArguments();
      typeArgumentNodes.forEach(NameResolver::resolve);
      occurrence.setIsGeneric();
      occurrence.setTypeArguments(
          typeArgumentNodes.stream().map(TypeNode::getType).collect(Collectors.toList()));
    }
    return occurrence;
  }

  private boolean nameResolutionFailed() {
    return names.size() != resolvedDeclarations.size() + Math.min(1, declarations.size());
  }

  private void specializeDeclarations(DelphiNameOccurrence occurrence) {
    declarations =
        declarations.stream()
            .map(DelphiNameDeclaration.class::cast)
            .map(
                declaration -> {
                  List<Type> typeArguments = occurrence.getTypeArguments();
                  var context = new TypeSpecializationContext(declaration, typeArguments);
                  return declaration.specialize(context);
                })
            .collect(Collectors.toSet());
  }

  private boolean isExplicitArrayConstructorInvocation(NameReferenceNode reference) {
    return getLast(resolvedDeclarations, null) instanceof TypeNameDeclaration
        && currentType.isDynamicArray()
        && reference.getLastName().hasImageEqualTo("Create");
  }

  private void readPossibleUnitNameReference(NameReferenceNode node) {
    DefaultNameResolver unitNameResolver = new DefaultNameResolver();
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

    if (declarations.stream().anyMatch(DefaultNameResolver::isArrayProperty)) {
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
    NameResolver.resolve(parenthesized);
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
        DefaultNameResolver propertyResolver = new DefaultNameResolver(this);
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
    NameResolver.resolve(argumentExpressions.get(0));
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
      NameResolver.resolveSubExpressions(argument);
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

    if (names.isEmpty() || !getLast(names).isGeneric()) {
      createImplicitSpecializationCandidates(resolver);
    }

    resolver.processCandidates();
    Set<InvocationCandidate> bestCandidate = resolver.chooseBest();

    declarations =
        bestCandidate.stream()
            .map(InvocationCandidate::getData)
            .map(NameDeclaration.class::cast)
            .collect(Collectors.toSet());

    disambiguateDistanceFromCallSite();
    disambiguateRegularMethodOverImplicitSpecializations();

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
      Parameter parameter = invocable.getParameter(i);
      argument.resolve(parameter.getType());
    }

    resolveReturnType(invocable, resolver.getArguments());
  }

  private void resolveReturnType(Invocable invocable, List<InvocationArgument> arguments) {
    if (!isConstructor((NameDeclaration) invocable)) {
      Type returnType = invocable.getReturnType();
      if (returnType instanceof IntrinsicReturnType) {
        List<Type> argumentTypes =
            arguments.stream()
                .map(InvocationArgument::getType)
                .collect(Collectors.toUnmodifiableList());
        returnType = ((IntrinsicReturnType) returnType).getReturnType(argumentTypes);
      }
      updateType(returnType);
    }
  }

  private void createImplicitSpecializationCandidates(InvocationResolver resolver) {
    List<Type> argumentTypes =
        resolver.getArguments().stream()
            .map(DefaultNameResolver::getArgumentTypeForImplicitSpecialization)
            .collect(Collectors.toList());

    for (NameDeclaration declaration : declarations) {
      var specialized = InvocationCandidate.implicitSpecialization(declaration, argumentTypes);
      if (specialized != null) {
        resolver.addCandidate(specialized);
      }
    }
  }

  private static Type getArgumentTypeForImplicitSpecialization(InvocationArgument argument) {
    Type type = argument.getType();
    if (argument.looksLikeMethodReference()) {
      Type returnType = ((ProceduralType) type).returnType();
      if (!returnType.is(voidType())) {
        type = returnType;
      }
    }
    return type;
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

  void disambiguateParameters(List<Type> parameterTypes) {
    disambiguateInvocable();
    disambiguateArity(parameterTypes.size());

    declarations.removeIf(
        declaration -> {
          List<Parameter> parameters = ((Invocable) declaration).getParameters();
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

  void disambiguateReturnType(Type returnType) {
    if (!returnType.isUnknown()) {
      disambiguateInvocable();
      declarations.removeIf(
          declaration -> !((Invocable) declaration).getReturnType().is(returnType));
    }
  }

  void disambiguateIsClassInvocable(boolean isClassInvocable) {
    declarations.removeIf(
        declaration -> ((Invocable) declaration).isClassInvocable() != isClassInvocable);
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

  private void disambiguateExplicitGeneric(int argumentCount) {
    declarations.removeIf(
        declaration -> {
          if (declaration instanceof GenerifiableDeclaration) {
            GenerifiableDeclaration generifiable = (GenerifiableDeclaration) declaration;
            return generifiable.getTypeParameters().size() != argumentCount;
          }
          return true;
        });
  }

  private void disambiguateNotGeneric() {
    declarations.removeIf(
        declaration -> {
          if (declaration instanceof MethodNameDeclaration) {
            // Could be an implicit specialization
            return false;
          }

          if (declaration instanceof GenerifiableDeclaration) {
            GenerifiableDeclaration generifiable = (GenerifiableDeclaration) declaration;
            return generifiable.isGeneric();
          }

          return false;
        });
  }

  /**
   * If overload selection between a generic method and a non-generic method would otherwise be
   * ambiguous, the non-generic method is selected.
   *
   * @see <a href="bit.ly/overloads-type-compatibility-generics">Overloads and Type Compatibility in
   *     Generics</a>
   */
  private void disambiguateRegularMethodOverImplicitSpecializations() {
    if (declarations.size() > 1
        && declarations.stream().allMatch(MethodNameDeclaration.class::isInstance)) {
      Set<NameDeclaration> nonGenericDeclarations =
          declarations.stream()
              .map(MethodNameDeclaration.class::cast)
              .filter(not(MethodNameDeclaration::isGeneric))
              .collect(Collectors.toSet());

      if (nonGenericDeclarations.size() == 1) {
        declarations = nonGenericDeclarations;
      }
    }
  }

  private void disambiguateDistanceFromCallSite() {
    if (declarations.size() > 1
        && declarations.stream().allMatch(MethodNameDeclaration.class::isInstance)) {

      Set<TypeNameDeclaration> methodTypes =
          declarations.stream()
              .map(MethodNameDeclaration.class::cast)
              .map(MethodNameDeclaration::getTypeDeclaration)
              .collect(Collectors.toSet());

      if (methodTypes.contains(null)) {
        disambiguateDistanceFromUnit();
      } else {
        disambiguateDistanceFromType();
      }
    }
  }

  private void disambiguateDistanceFromUnit() {
    String currentUnit = getLast(names).getLocation().getUnitName();
    Set<MethodNameDeclaration> methodDeclarations =
        declarations.stream().map(MethodNameDeclaration.class::cast).collect(Collectors.toSet());

    if (methodDeclarations.stream()
        .map(DelphiNameDeclaration::getNode)
        .map(SymbolicNode::getUnitName)
        .anyMatch(currentUnit::equals)) {
      declarations =
          methodDeclarations.stream()
              .filter(declaration -> declaration.getNode().getUnitName().equals(currentUnit))
              .collect(Collectors.toSet());
    }
  }

  private void disambiguateDistanceFromType() {
    Set<MethodNameDeclaration> methodDeclarations =
        declarations.stream().map(MethodNameDeclaration.class::cast).collect(Collectors.toSet());

    Type closestType =
        methodDeclarations.stream()
            .map(MethodNameDeclaration::getTypeDeclaration)
            .filter(Objects::nonNull)
            .map(TypeNameDeclaration::getType)
            .max(DefaultNameResolver::compareTypeSpecificity)
            .orElseThrow();

    declarations =
        methodDeclarations.stream()
            .filter(
                declaration -> {
                  TypeNameDeclaration typeDeclaration = declaration.getTypeDeclaration();
                  return Objects.requireNonNull(typeDeclaration).getType().is(closestType);
                })
            .collect(Collectors.toSet());
  }

  private static int compareTypeSpecificity(Type typeA, Type typeB) {
    if (typeA.is(typeB)) {
      return 0;
    } else if (typeA.isSubTypeOf(typeB) || hasHelperRelationship(typeA, typeB)) {
      return 1;
    } else {
      return -1;
    }
  }

  private static boolean hasHelperRelationship(Type typeA, Type typeB) {
    if (typeA.isHelper()) {
      Type extended = ((HelperType) typeA).extendedType();
      return extended.is(typeB) || extended.isSubTypeOf(typeB);
    }
    return false;
  }

  private void disambiguateWithinUnit(String unitName) {
    declarations.removeIf(
        declaration ->
            !((DelphiNameDeclaration) declaration).getNode().getUnitName().equals(unitName));
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
    if (currentType.isTypeParameter()) {
      searchForDeclarationInConstraintTypes(occurrence);
      return;
    }

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
   * Embarcadero claims that finding declarations in constrained types works by creating a "union"
   * of declarations from the constraint types and then performing a "variation of overload
   * resolution" on that union.
   *
   * <p>Oops, turns out that was a lie. The compiler actually does an individual search through each
   * constraint type, and stops once it finds any declaration that might match the name reference.
   * Constraints are searched in the order that they were declared on the type parameter.
   *
   * <p>This is an objectively horrible way for the compiler to do the search, especially when you
   * consider that they clearly had a better implementation in mind.
   *
   * @param occurrence The name occurrence we're currently searching for
   * @see <a href="bit.ly/constraints-in-generics-type-inferencing">Constraints in Generics: Type
   *     Inferencing</a>
   */
  private void searchForDeclarationInConstraintTypes(DelphiNameOccurrence occurrence) {
    TypeParameterType type = (TypeParameterType) currentType;
    for (Type constraint : type.constraints()) {
      if (constraint instanceof ScopedType) {
        currentType = constraint;
        currentScope = ((ScopedType) constraint).typeScope();
        searchForDeclaration(occurrence);
        if (!declarations.isEmpty()) {
          break;
        }
      }
    }
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

  private static void registerOccurrence(DelphiNameOccurrence occurrence) {
    occurrence
        .getLocation()
        .getScope()
        .getEnclosingScope(FileScope.class)
        .registerOccurrence(occurrence.getLocation(), occurrence);
  }

  private static class DisambiguationException extends RuntimeException {
    DisambiguationException(Set<NameDeclaration> declarations, DelphiNameOccurrence occurrence) {
      super(
          "Ambiguous declarations could not be resolved\n[Occurrence]  "
              + occurrence
              + "\n[Declaration] "
              + StringUtils.join(declarations, "\n[Declaration] "));
    }
  }
}
