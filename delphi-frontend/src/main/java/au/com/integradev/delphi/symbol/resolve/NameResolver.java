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
package au.com.integradev.delphi.symbol.resolve;

import static au.com.integradev.delphi.symbol.resolve.EqualityType.INCOMPATIBLE_TYPES;
import static java.util.function.Predicate.not;
import static org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope.unknownScope;
import static org.sonar.plugins.communitydelphi.api.type.TypeFactory.unknownType;
import static org.sonar.plugins.communitydelphi.api.type.TypeFactory.voidType;

import au.com.integradev.delphi.antlr.ast.node.ArrayAccessorNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.DelphiNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.NameReferenceNodeImpl;
import au.com.integradev.delphi.symbol.Search;
import au.com.integradev.delphi.symbol.SearchMode;
import au.com.integradev.delphi.symbol.SymbolicNode;
import au.com.integradev.delphi.symbol.declaration.TypeParameterNameDeclarationImpl;
import au.com.integradev.delphi.symbol.occurrence.AttributeNameOccurrenceImpl;
import au.com.integradev.delphi.symbol.occurrence.NameOccurrenceImpl;
import au.com.integradev.delphi.symbol.scope.DelphiScopeImpl;
import au.com.integradev.delphi.symbol.scope.FileScopeImpl;
import au.com.integradev.delphi.type.TypeUtils;
import au.com.integradev.delphi.type.UnresolvedTypeImpl;
import au.com.integradev.delphi.type.factory.StructTypeImpl;
import au.com.integradev.delphi.type.generic.TypeParameterTypeImpl;
import au.com.integradev.delphi.type.generic.TypeSpecializationContextImpl;
import au.com.integradev.delphi.type.intrinsic.IntrinsicReturnType;
import com.google.common.base.Preconditions;
import com.google.common.base.Suppliers;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayAccessorNode;
import org.sonar.plugins.communitydelphi.api.ast.AttributeNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.GenericArgumentsNode;
import org.sonar.plugins.communitydelphi.api.ast.IdentifierNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.ParenthesizedExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.symbol.Invocable;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.GenerifiableDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.QualifiedNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeParameterNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.RoutineScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.TypeScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.UnknownScope;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ClassReferenceType;
import org.sonar.plugins.communitydelphi.api.type.Type.CollectionType;
import org.sonar.plugins.communitydelphi.api.type.Type.HelperType;
import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;
import org.sonar.plugins.communitydelphi.api.type.Type.StringType;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;
import org.sonar.plugins.communitydelphi.api.type.Type.TypeParameterType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;
import org.sonar.plugins.communitydelphi.api.type.Typed;

public class NameResolver {
  private static final Logger LOG = LoggerFactory.getLogger(NameResolver.class);
  private final TypeFactory typeFactory;
  private final List<NameOccurrenceImpl> names = new ArrayList<>();
  private final List<NameDeclaration> resolvedDeclarations = new ArrayList<>();
  private final SearchMode searchMode;
  private final Supplier<NameResolutionHelper> nameResolutionHelper;
  private Set<NameDeclaration> declarations = new HashSet<>();
  private DelphiScope currentScope;
  private Type currentType = unknownType();

  NameResolver(TypeFactory typeFactory) {
    this(typeFactory, SearchMode.DEFAULT);
  }

  NameResolver(TypeFactory typeFactory, SearchMode searchMode) {
    this.typeFactory = typeFactory;
    this.searchMode = searchMode;
    this.nameResolutionHelper =
        Suppliers.memoize(() -> new NameResolutionHelper(getTypeFactory(), searchMode));
  }

  NameResolver(NameResolver resolver) {
    this(resolver.typeFactory, resolver.searchMode);
    declarations.addAll(resolver.declarations);
    currentScope = resolver.currentScope;
    currentType = resolver.currentType;
    names.addAll(resolver.names);
    resolvedDeclarations.addAll(resolver.resolvedDeclarations);
  }

  private TypeFactory getTypeFactory() {
    return typeFactory;
  }

  private NameResolutionHelper getNameResolutionHelper() {
    return nameResolutionHelper.get();
  }

  public Type getApproximateType() {
    if (declarations.size() + resolvedDeclarations.size() < names.size()) {
      return unknownType();
    }

    if (!declarations.isEmpty()) {
      NameDeclaration declaration = Iterables.getLast(declarations);
      if (declaration instanceof TypedDeclaration) {
        return findTypeForTypedDeclaration((TypedDeclaration) declaration);
      }
      return unknownType();
    }

    return currentType;
  }

  public Set<NameDeclaration> getDeclarations() {
    return declarations;
  }

  public List<NameDeclaration> getResolvedDeclarations() {
    return resolvedDeclarations;
  }

  NameDeclaration addResolvedDeclaration() {
    if (declarations.isEmpty()) {
      return null;
    }

    NameDeclaration resolved = Iterables.getLast(declarations);
    checkAmbiguity();
    currentScope = unknownScope();

    if (declarations.size() == 1) {
      NameDeclaration declaration = Iterables.getLast(declarations);
      if (declaration instanceof TypedDeclaration) {
        updateType(findTypeForTypedDeclaration((TypedDeclaration) declaration));
      } else if (declaration instanceof UnitImportNameDeclaration) {
        FileScope unitScope = ((UnitImportNameDeclaration) declaration).getUnitScope();
        if (unitScope != null) {
          currentScope = unitScope;
        }
      } else if (declaration instanceof UnitNameDeclaration) {
        currentScope = ((UnitNameDeclaration) declaration).getFileScope();
      }
    }

    resolvedDeclarations.addAll(declarations);
    declarations.clear();
    return resolved;
  }

  public void checkAmbiguity() {
    if (declarations.size() > 1) {
      if (LOG.isWarnEnabled()) {
        LOG.warn(
            "Ambiguous declarations could not be resolved\n[Occurrence] {}\n{}",
            Iterables.getLast(names),
            declarations.stream()
                .map(declaration -> "[Declaration] " + declaration)
                .collect(Collectors.joining("\n")));
      }
      declarations.clear();
    }
  }

  void updateType(Type type) {
    currentType = type;
    ScopedType scopedType = extractScopedType(currentType);
    DelphiScope newScope = unknownScope();
    if (scopedType != null) {
      newScope = scopedType.typeScope();
    }
    currentScope = newScope;
  }

  public boolean isExplicitInvocation() {
    return !names.isEmpty() && Iterables.getLast(names).isExplicitInvocation();
  }

  public void addToSymbolTable() {
    addResolvedDeclaration();

    for (int i = 0; i < resolvedDeclarations.size(); ++i) {
      NameOccurrenceImpl name = names.get(i);
      NameDeclaration declaration = resolvedDeclarations.get(i);
      name.setNameDeclaration(declaration);

      ((DelphiScopeImpl) declaration.getScope()).addNameOccurrence(name);
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

      if (result.isUnknown() && names.size() == 1) {
        DelphiScope scope = Iterables.getLast(names).getLocation().getScope();
        result = findCurrentType(scope);
      }
    } else {
      result = declaration.getType();
      if (isTypeIdentifier(declaration)) {
        result = typeFactory.classOf(null, result);
      }
    }

    return result;
  }

  private static Type findCurrentType(DelphiScope scope) {
    scope = Objects.requireNonNullElse(scope, unknownScope());
    RoutineScope routineScope = scope.getEnclosingScope(RoutineScope.class);
    if (routineScope != null) {
      DelphiScope typeScope = routineScope.getTypeScope();
      if (typeScope instanceof TypeScope) {
        return ((TypeScope) typeScope).getType();
      }
      return findCurrentType(routineScope.getParent());
    }
    return unknownType();
  }

  private static boolean isConstructor(NameDeclaration declaration) {
    return declaration instanceof RoutineNameDeclaration
        && ((RoutineNameDeclaration) declaration).getRoutineKind() == RoutineKind.CONSTRUCTOR;
  }

  void readPrimaryExpression(PrimaryExpressionNode node) {
    if (handleInheritedExpression(node)) {
      return;
    }

    for (DelphiNode child : node.getChildren()) {
      if (!readPrimaryExpressionPart(child)) {
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
    } else if (node instanceof Typed) {
      updateType(((Typed) node).getType());
    } else {
      handlePrimaryExpressionToken(node);
    }

    return !nameResolutionFailed();
  }

  private void handlePrimaryExpressionToken(Node node) {
    switch (node.getTokenType()) {
      case DEREFERENCE:
        Type dereferenced = TypeUtils.dereference(getApproximateType());
        addResolvedDeclaration();
        updateType(dereferenced);
        break;

      case STRING:
        updateType(
            typeFactory.classOf(null, typeFactory.getIntrinsic(IntrinsicType.UNICODESTRING)));
        break;

      case FILE:
        updateType(typeFactory.classOf(null, typeFactory.untypedFile()));
        break;

      default:
        // Do nothing
    }
  }

  private boolean handleInheritedExpression(PrimaryExpressionNode node) {
    if (!ExpressionNodeUtils.isInherited(node)) {
      return false;
    }

    moveToInheritedScope(node);

    if (ExpressionNodeUtils.isBareInherited(node)) {
      RoutineImplementationNode routine =
          node.getFirstParentOfType(RoutineImplementationNode.class);
      DelphiNode inheritedNode = node.getChild(0);

      NameOccurrenceImpl occurrence = new NameOccurrenceImpl(inheritedNode, routine.simpleName());
      occurrence.setIsExplicitInvocation(true);
      addName(occurrence);
      searchForDeclaration(occurrence);
      disambiguateIsCallable();
      disambiguateVisibility();
      disambiguateParameters(routine.getParameterTypes());
      addResolvedDeclaration();
    } else {
      NameReferenceNode routineName = (NameReferenceNode) node.getChild(1);
      NameOccurrenceImpl occurrence = new NameOccurrenceImpl(routineName.getIdentifier());
      addName(occurrence);

      declarations = currentScope.findDeclaration(occurrence);

      if (declarations.isEmpty() && currentScope instanceof TypeScope) {
        currentScope = ((TypeScope) currentScope).getParentTypeScope();
        searchForDeclaration(occurrence);
      }

      disambiguateIsCallable();
      disambiguateVisibility();

      NameReferenceNode nextName = routineName.nextName();
      if (!declarations.isEmpty()) {
        ((NameReferenceNodeImpl) routineName).setNameOccurrence(occurrence);
        if (nextName != null) {
          disambiguateImplicitEmptyArgumentList();
          addResolvedDeclaration();
          readNameReference(nextName);
        }
      }
    }

    int nextChild = ExpressionNodeUtils.isBareInherited(node) ? 1 : 2;

    for (int i = nextChild; i < node.getChildren().size(); ++i) {
      if (!readPrimaryExpressionPart(node.getChild(i))) {
        break;
      }
    }

    return true;
  }

  private void moveToInheritedScope(PrimaryExpressionNode node) {
    RoutineImplementationNode routine = node.getFirstParentOfType(RoutineImplementationNode.class);
    Preconditions.checkNotNull(routine);

    currentScope = node.getScope();

    TypeNameDeclaration typeDeclaration = routine.getTypeDeclaration();
    if (typeDeclaration != null) {
      Type type = typeDeclaration.getType();
      Type newType = type.parent();

      // Rules for inherited statements in helper methods are a bit unintuitive.
      // Inheritance in helpers doesn't work in the same way as classes. It's more like aggregation.
      //
      // For starters, we ignore any helper ancestors and go straight into the extended type.
      // If this is a bare inherited call with no specified method signature, then we even skip the
      // extended type and move into its parent.
      // See: https://wiki.freepascal.org/Helper_types#Inherited_with_function_name
      if (type.isHelper()) {
        newType = ((HelperType) type).extendedType();
        if (ExpressionNodeUtils.isBareInherited(node)) {
          newType = newType.parent();
        }
      }

      if (newType instanceof ScopedType) {
        currentScope = ((ScopedType) newType).typeScope();
      } else {
        currentScope = unknownScope();
      }
    }
  }

  void readRoutineNameInterfaceReference(NameReferenceNode node) {
    currentScope = node.getScope().getParent();

    for (NameReferenceNode reference : node.flatten()) {
      IdentifierNode identifier = reference.getIdentifier();
      NameOccurrenceImpl occurrence = new NameOccurrenceImpl(identifier);

      GenericArgumentsNode genericArguments = reference.getGenericArguments();
      List<TypeReferenceNode> typeArguments = Collections.emptyList();

      if (genericArguments != null) {
        typeArguments = genericArguments.findChildrenOfType(TypeReferenceNode.class);
        occurrence.setIsGeneric();
        // Unresolved type arguments are created and added to the name occurrence so we can find
        // declarations properly based on the number of type arguments.
        // These temporary type arguments are overwritten with their actual types once the
        // declaration has been found.
        occurrence.setTypeArguments(
            genericArguments.findChildrenOfType(TypeReferenceNode.class).stream()
                .map(TypeReferenceNode::getNameNode)
                .map(Node::getImage)
                .map(UnresolvedTypeImpl::referenceTo)
                .collect(Collectors.toUnmodifiableList()));
      }

      addName(occurrence);

      // Routine name interface references should be resolved quite literally.
      // If we allow it to go through the more general name resolution steps and scope traversals,
      // we could end up inside a class helper or parent type or something.
      declarations = currentScope.findDeclaration(occurrence);
      declarations.removeIf(declaration -> declaration.getScope() != currentScope);

      if (occurrence.isGeneric()) {
        if (reference.nextName() != null) {
          // If we're in the middle of the name, then we assume that we have already properly
          // disambiguated the reference, and resolve the type parameter references back to their
          // declarations on that type reference
          handleTypeParameterReferences(typeArguments);
        } else {
          // If we're on the last name, then it's the routine reference.
          // It's impossible for us to know which routine we're referring to before parameter
          // resolution occurs.
          // Instead, we create temporary type parameter declarations that will live in the routine
          // scope as "forward" declarations. Once parameter resolution has occurred, we can come
          // back and complete the type parameter declaration/type.
          RoutineScope routineScope = node.getScope().getEnclosingScope(RoutineScope.class);
          handleTypeParameterForwardReferences(routineScope, typeArguments);
        }

        occurrence.setTypeArguments(
            typeArguments.stream()
                .map(TypeReferenceNode::getType)
                .collect(Collectors.toUnmodifiableList()));
      } else {
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

      ((NameReferenceNodeImpl) reference).setNameOccurrence(occurrence);
    }
  }

  private void handleTypeParameterReferences(List<TypeReferenceNode> typeReferences) {
    NameDeclaration currentDeclaration = Iterables.getLast(declarations, null);
    if (!(currentDeclaration instanceof GenerifiableDeclaration)) {
      return;
    }

    GenerifiableDeclaration generifiable = (GenerifiableDeclaration) currentDeclaration;
    List<TypedDeclaration> typeParameters = generifiable.getTypeParameters();

    for (int i = 0; i < typeParameters.size(); ++i) {
      TypedDeclaration declaration = typeParameters.get(i);
      NameReferenceNode typeReference = typeReferences.get(i).getNameNode();
      NameOccurrenceImpl occurrence = new NameOccurrenceImpl(typeReference);

      occurrence.setNameDeclaration(declaration);
      ((NameReferenceNodeImpl) typeReference).setNameOccurrence(occurrence);

      NameResolver resolver =
          new NameResolver(((DelphiNodeImpl) typeReference).getTypeFactory(), searchMode);
      resolver.resolvedDeclarations.add(declaration);
      resolver.names.add(occurrence);
      resolver.addToSymbolTable();
    }
  }

  private void handleTypeParameterForwardReferences(
      RoutineScope routineScope, List<TypeReferenceNode> typeReferences) {
    for (TypeReferenceNode typeNode : typeReferences) {
      NameReferenceNode typeReference = typeNode.getNameNode();
      TypeParameterType type = TypeParameterTypeImpl.create(typeReference.getImage());
      NameDeclaration declaration = new TypeParameterNameDeclarationImpl(typeReference, type);
      ((DelphiScopeImpl) routineScope).addDeclaration(declaration);

      NameOccurrenceImpl occurrence = new NameOccurrenceImpl(typeReference);
      occurrence.setNameDeclaration(declaration);
      ((NameReferenceNodeImpl) typeReference).setNameOccurrence(occurrence);

      NameResolver resolver =
          new NameResolver(((DelphiNodeImpl) typeNode).getTypeFactory(), searchMode);
      resolver.resolvedDeclarations.add(declaration);
      resolver.names.add(occurrence);
      resolver.addToSymbolTable();
    }
  }

  void readAttribute(AttributeNode node) {
    readNameReference(node.getNameReference(), true);
    addResolvedDeclaration();

    List<NameReferenceNode> references = node.getNameReference().flatten();
    NameReferenceNode lastReference = references.get(references.size() - 1);

    var attributeNameOccurrence = (AttributeNameOccurrenceImpl) lastReference.getNameOccurrence();
    if (attributeNameOccurrence == null) {
      return;
    }

    // Attributes are a shorthand for an invocation of a constructor named 'Create'
    SymbolicNode imaginaryConstructor = SymbolicNode.imaginary("Create", node.getScope());
    NameOccurrenceImpl occurrence = new NameOccurrenceImpl(imaginaryConstructor);
    attributeNameOccurrence.setImplicitConstructorNameOccurrence(occurrence);

    resolveNameReferenceOccurrence(occurrence, true);

    ArgumentListNode argumentList = node.getArgumentList();
    if (argumentList != null) {
      handleArgumentList(argumentList);
    } else {
      disambiguateArguments(Collections.emptyList(), true);
    }
  }

  private void readNameReference(NameReferenceNode node, boolean inAttribute) {
    boolean couldBeUnitNameReference =
        currentScope == null
            || (!(currentScope instanceof UnknownScope) && currentScope.equals(node.getScope()));

    for (NameReferenceNode reference : node.flatten()) {
      if (isExplicitArrayConstructorInvocation(reference)) {
        return;
      }

      NameOccurrence occurrence = createNameOccurrence(reference, inAttribute);
      resolveNameReferenceOccurrence(occurrence, reference.nextName() == null);

      if (nameResolutionFailed()) {
        if (couldBeUnitNameReference) {
          readPossibleUnitNameReference(node, inAttribute);
        }
        return;
      }

      ((NameReferenceNodeImpl) reference).setNameOccurrence(occurrence);
    }
  }

  private void resolveNameReferenceOccurrence(NameOccurrence occurrence, boolean isLastName) {
    addName((NameOccurrenceImpl) occurrence);
    searchForDeclaration(occurrence);

    if (occurrence.isGeneric()) {
      specializeDeclarations(occurrence);
    }

    disambiguateIsCallable();
    disambiguateVisibility();
    disambiguateUnitImport();

    if (!isLastName) {
      disambiguateImplicitEmptyArgumentList();
      addResolvedDeclaration();
    }
  }

  void readNameReference(NameReferenceNode node) {
    readNameReference(node, false);
  }

  private NameOccurrence createNameOccurrence(NameReferenceNode reference, boolean attribute) {
    DelphiNode node = reference.getIdentifier();
    NameOccurrenceImpl occurrence =
        attribute && reference.nextName() == null
            ? new AttributeNameOccurrenceImpl(node)
            : new NameOccurrenceImpl(node);

    GenericArgumentsNode genericArguments = reference.getGenericArguments();
    if (genericArguments != null) {
      List<TypeNode> typeArgumentNodes = genericArguments.getTypeArguments();
      typeArgumentNodes.forEach(getNameResolutionHelper()::resolve);
      occurrence.setIsGeneric();
      occurrence.setTypeArguments(
          typeArgumentNodes.stream().map(TypeNode::getType).collect(Collectors.toList()));
    }

    return occurrence;
  }

  private boolean nameResolutionFailed() {
    return names.size() != resolvedDeclarations.size() + Math.min(1, declarations.size());
  }

  private void specializeDeclarations(NameOccurrence occurrence) {
    declarations =
        declarations.stream()
            .map(NameDeclaration.class::cast)
            .map(
                declaration -> {
                  List<Type> typeArguments = occurrence.getTypeArguments();
                  var context = new TypeSpecializationContextImpl(declaration, typeArguments);
                  return declaration.specialize(context);
                })
            .collect(Collectors.toSet());
  }

  private boolean isExplicitArrayConstructorInvocation(NameReferenceNode reference) {
    return Iterables.getLast(resolvedDeclarations, null) instanceof TypeNameDeclaration
        && isDynamicArrayReference(currentType)
        && reference.getLastName().getImage().equalsIgnoreCase("Create");
  }

  private static boolean isDynamicArrayReference(Type type) {
    return type.isClassReference() && ((ClassReferenceType) type).classType().isDynamicArray();
  }

  private void readPossibleUnitNameReference(NameReferenceNode node, boolean inAttribute) {
    NameResolver unitNameResolver = new NameResolver(((DelphiNodeImpl) node).getTypeFactory());
    if (unitNameResolver.readUnitNameReference(node, inAttribute)) {
      this.currentType = unknownType();
      this.names.clear();
      this.resolvedDeclarations.clear();

      this.currentScope = unitNameResolver.currentScope;
      this.declarations = unitNameResolver.declarations;
      this.names.addAll(unitNameResolver.names);
      this.resolvedDeclarations.addAll(unitNameResolver.resolvedDeclarations);
    }
  }

  private boolean readUnitNameReference(NameReferenceNode node, boolean inAttribute) {
    FileScope fileScope = node.getScope().getEnclosingScope(FileScope.class);

    List<QualifiedNameDeclaration> unitDeclarations = new ArrayList<>();
    unitDeclarations.addAll(fileScope.getUnitDeclarations());
    unitDeclarations.addAll(fileScope.getImportDeclarations());
    unitDeclarations.sort(Comparator.comparing(NameDeclaration::getImage).reversed());

    for (QualifiedNameDeclaration declaration : unitDeclarations) {
      if (matchReferenceToUnitNameDeclaration(node, declaration, inAttribute)) {
        return true;
      }
    }
    return false;
  }

  private boolean matchReferenceToUnitNameDeclaration(
      NameReferenceNode node, QualifiedNameDeclaration declaration, boolean inAttribute) {
    List<String> declarationParts = declaration.getQualifiedName().parts();
    List<NameReferenceNode> references = node.flatten();

    if (declarationParts.size() > references.size()) {
      return false;
    }

    for (int i = 0; i < declarationParts.size(); ++i) {
      if (!references.get(i).getIdentifier().getImage().equalsIgnoreCase(declarationParts.get(i))) {
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

    NameOccurrenceImpl occurrence = new NameOccurrenceImpl(symbolicNode);
    ((NameReferenceNodeImpl) node).setNameOccurrence(occurrence);
    addName(occurrence);
    declarations.add(declaration);
    addResolvedDeclaration();

    if (references.size() > declarationParts.size()) {
      readNameReference(references.get(declarationParts.size()), inAttribute);
    }

    return true;
  }

  private void handleArrayAccessor(ArrayAccessorNode accessor) {
    Type type = TypeUtils.findBaseType(getApproximateType());
    if (type.isPointer()) {
      Type dereferenced = TypeUtils.dereference(type);
      addResolvedDeclaration();
      updateType(dereferenced);
    }

    if (handleDefaultArrayProperties(accessor)) {
      return;
    }

    if (declarations.stream().anyMatch(NameResolver::isArrayProperty)) {
      disambiguateArguments(accessor.getExpressions(), true);
      addResolvedDeclaration();
      return;
    }

    addResolvedDeclaration();
    if (currentType.isProcedural()) {
      updateType(TypeUtils.dereference(((ProceduralType) currentType).returnType()));
    }

    accessor.getExpressions().forEach(getNameResolutionHelper()::resolve);

    for (int i = 0; i < accessor.getExpressions().size(); ++i) {
      if (currentType.isArray()) {
        updateType(((CollectionType) currentType).elementType());
      } else if (currentType.isString()) {
        updateType(((StringType) currentType).characterType());
      }
    }
  }

  private void handleParenthesizedExpression(ParenthesizedExpressionNode parenthesized) {
    getNameResolutionHelper().resolve(parenthesized);
    updateType(parenthesized.getType());

    ScopedType type = extractScopedType(currentType);
    if (type != null) {
      currentScope = type.typeScope();
    } else {
      currentScope = unknownScope();
    }
  }

  private boolean handleDefaultArrayProperties(ArrayAccessorNode accessor) {
    if (!declarations.isEmpty() && isArrayProperty(Iterables.getLast(declarations))) {
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
      StructType structType = (StructType) TypeUtils.findBaseType(type);
      var defaultArrayProperties = ((StructTypeImpl) structType).findDefaultArrayProperties();
      if (!defaultArrayProperties.isEmpty()) {
        NameResolver propertyResolver = new NameResolver(this);
        propertyResolver.declarations = defaultArrayProperties;
        propertyResolver.disambiguateArguments(accessor.getExpressions(), true);

        NameDeclaration propertyDeclaration =
            Iterables.getLast(propertyResolver.resolvedDeclarations);
        if (propertyDeclaration instanceof PropertyNameDeclaration) {
          NameOccurrenceImpl implicitOccurrence = new NameOccurrenceImpl(accessor);
          implicitOccurrence.setNameDeclaration(propertyDeclaration);
          ((ArrayAccessorNodeImpl) accessor).setImplicitNameOccurrence(implicitOccurrence);
          ((DelphiScopeImpl) propertyDeclaration.getScope()).addNameOccurrence(implicitOccurrence);
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
    if (declarations.stream().noneMatch(RoutineNameDeclaration.class::isInstance)) {
      return;
    }
    disambiguateArguments(Collections.emptyList(), false);
  }

  private void handleArgumentList(ArgumentListNode node) {
    for (ArgumentNode argument : node.getArgumentNodes()) {
      ExpressionNode width = argument.getWidth();
      if (width != null) {
        getNameResolutionHelper().resolve(width);
      }

      ExpressionNode decimals = argument.getDecimals();
      if (decimals != null) {
        getNameResolutionHelper().resolve(decimals);
      }
    }

    if (handleExplicitArrayConstructorInvocation(node)) {
      return;
    }

    disambiguateArguments(
        node.getArgumentNodes().stream()
            .map(ArgumentNode::getExpression)
            .collect(Collectors.toUnmodifiableList()),
        true);
  }

  private boolean handleExplicitArrayConstructorInvocation(ArgumentListNode node) {
    Node previous = node.getParent().getChild(node.getChildIndex() - 1);
    if (previous instanceof NameReferenceNode
        && isExplicitArrayConstructorInvocation(((NameReferenceNode) previous))) {
      updateType(((ClassReferenceType) currentType).classType());
      node.getArgumentNodes().stream()
          .map(ArgumentNode::getExpression)
          .forEach(getNameResolutionHelper()::resolve);
      return true;
    }
    return false;
  }

  private boolean handleHardTypeCast(List<ExpressionNode> argumentExpressions) {
    if (declarations.size() <= 1 && argumentExpressions.size() == 1) {
      if (declarations.size() == 1 && isTypeIdentifier(Iterables.getLast(declarations))) {
        addResolvedDeclaration();
      }

      if (declarations.isEmpty() && currentType.isClassReference()) {
        updateType(((ClassReferenceType) currentType).classType());
        getNameResolutionHelper().resolve(argumentExpressions.get(0));
        return true;
      }
    }
    return false;
  }

  private static boolean isTypeIdentifier(NameDeclaration declaration) {
    return declaration instanceof TypeNameDeclaration
        || declaration instanceof TypeParameterNameDeclaration;
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
      NameDeclaration declaration = Iterables.getLast(declarations);
      if (!(declaration instanceof VariableNameDeclaration
          || declaration instanceof PropertyNameDeclaration)) {
        return false;
      }

      Type variableType = ((Typed) Iterables.getLast(declarations)).getType();
      if (!variableType.isProcedural()) {
        return false;
      }

      proceduralType = (ProceduralType) variableType;
      addResolvedDeclaration();
    }

    List<Parameter> parameters = proceduralType.parameters();
    int count = Math.min(argumentExpressions.size(), parameters.size());

    for (int i = 0; i < count; ++i) {
      ExpressionNode argument = argumentExpressions.get(i);
      getNameResolutionHelper().resolveSubExpressions(argument);
      InvocationArgument invocationArgument = new InvocationArgument(argument);
      invocationArgument.resolve(parameters.get(i).getType());
    }

    updateType(proceduralType.returnType());
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

    argumentExpressions.forEach(getNameResolutionHelper()::resolveSubExpressions);

    InvocationResolver resolver = new InvocationResolver();
    argumentExpressions.stream().map(InvocationArgument::new).forEach(resolver::addArgument);
    createCandidates(resolver);

    resolver.processCandidates();
    Set<InvocationCandidate> bestCandidate = resolver.chooseBest();

    declarations =
        bestCandidate.stream()
            .map(InvocationCandidate::getData)
            .map(NameDeclaration.class::cast)
            .collect(Collectors.toSet());

    disambiguateDistanceFromCallSite();
    disambiguateRegularRoutineOverImplicitSpecializations();

    if (!names.isEmpty()) {
      Iterables.getLast(names).setIsExplicitInvocation(explicit);
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

  private void createCandidates(InvocationResolver resolver) {
    declarations.stream()
        .map(Invocable.class::cast)
        .forEach(invocable -> createCandidate(invocable, resolver));
  }

  private void createCandidate(Invocable invocable, InvocationResolver resolver) {
    InvocationCandidate candidate = null;
    boolean couldBeImplicitSpecialization =
        names.isEmpty() || !Iterables.getLast(names).isGeneric();

    if (couldBeImplicitSpecialization) {
      List<Type> argumentTypes =
          resolver.getArguments().stream()
              .map(NameResolver::getArgumentTypeForImplicitSpecialization)
              .collect(Collectors.toList());
      candidate = InvocationCandidate.implicitSpecialization(invocable, argumentTypes);
    }

    if (candidate == null) {
      candidate = new InvocationCandidate(invocable);
    }

    resolver.addCandidate(candidate);
  }

  private static Type getArgumentTypeForImplicitSpecialization(InvocationArgument argument) {
    Type type = argument.getType();
    if (argument.looksLikeProceduralReference()) {
      Type returnType = ((ProceduralType) type).returnType();
      if (!returnType.is(voidType())) {
        type = returnType;
      }
    }
    return type;
  }

  void disambiguateRoutineReference(ProceduralType procedure) {
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
  }

  void disambiguateAddressOfRoutineReference() {
    disambiguateInvocable();
    disambiguateIsCallable();

    NameDeclaration first = Iterables.getFirst(declarations, null);
    if (first != null) {
      declarations.clear();
      declarations.add(first);
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
    disambiguateInvocable();
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

  private void disambiguateUnitImport() {
    Set<UnitImportNameDeclaration> unitImports =
        declarations.stream()
            .filter(UnitImportNameDeclaration.class::isInstance)
            .map(UnitImportNameDeclaration.class::cast)
            .collect(Collectors.toSet());

    if (unitImports.isEmpty()) {
      return;
    }

    if (unitImports.stream().anyMatch(NameDeclaration::isImplementationDeclaration)) {
      // The implementation unit import is chosen over anything in the interface
      declarations.removeIf(not(NameDeclaration::isImplementationDeclaration));
    }

    if (unitImports.size() < declarations.size()) {
      // The unit import is shadowed by anything below it
      declarations.removeAll(unitImports);
    }
  }

  private void disambiguateNotGeneric() {
    declarations.removeIf(
        declaration -> {
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
  private void disambiguateRegularRoutineOverImplicitSpecializations() {
    if (declarations.size() > 1
        && declarations.stream().allMatch(RoutineNameDeclaration.class::isInstance)) {
      Set<NameDeclaration> nonGenericDeclarations =
          declarations.stream()
              .map(RoutineNameDeclaration.class::cast)
              .filter(not(RoutineNameDeclaration::isGeneric))
              .collect(Collectors.toSet());

      if (nonGenericDeclarations.size() == 1) {
        declarations = nonGenericDeclarations;
      }
    }
  }

  private void disambiguateDistanceFromCallSite() {
    if (declarations.size() > 1
        && declarations.stream().allMatch(RoutineNameDeclaration.class::isInstance)) {

      Set<TypeNameDeclaration> methodTypes =
          declarations.stream()
              .map(RoutineNameDeclaration.class::cast)
              .map(RoutineNameDeclaration::getTypeDeclaration)
              .collect(Collectors.toSet());

      if (methodTypes.contains(null)) {
        disambiguateDistanceFromUnit();
      } else {
        disambiguateDistanceFromType();
      }
    }
  }

  private void disambiguateDistanceFromUnit() {
    String currentUnit = Iterables.getLast(names).getLocation().getUnitName();
    Set<RoutineNameDeclaration> routineDeclarations =
        declarations.stream().map(RoutineNameDeclaration.class::cast).collect(Collectors.toSet());

    if (routineDeclarations.stream()
        .map(NameDeclaration::getNode)
        .map(Node::getUnitName)
        .anyMatch(currentUnit::equals)) {
      declarations =
          routineDeclarations.stream()
              .filter(declaration -> declaration.getNode().getUnitName().equals(currentUnit))
              .collect(Collectors.toSet());
    }
  }

  private void disambiguateDistanceFromType() {
    Set<RoutineNameDeclaration> routineDeclarations =
        declarations.stream().map(RoutineNameDeclaration.class::cast).collect(Collectors.toSet());

    Type closestType =
        routineDeclarations.stream()
            .map(RoutineNameDeclaration::getTypeDeclaration)
            .filter(Objects::nonNull)
            .map(TypeNameDeclaration::getType)
            .max(NameResolver::compareTypeSpecificity)
            .orElseThrow();

    declarations =
        routineDeclarations.stream()
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
    } else if (typeA.isDescendantOf(typeB) || hasHelperRelationship(typeA, typeB)) {
      return 1;
    } else {
      return -1;
    }
  }

  private static boolean hasHelperRelationship(Type typeA, Type typeB) {
    if (typeA.isHelper()) {
      Type extended = ((HelperType) typeA).extendedType();
      return extended.is(typeB) || extended.isDescendantOf(typeB);
    }
    return false;
  }

  private void disambiguateWithinUnit(String unitName) {
    declarations.removeIf(declaration -> !declaration.getNode().getUnitName().equals(unitName));
  }

  private boolean isVisibleDeclaration(NameDeclaration declaration) {
    if (declaration instanceof RoutineNameDeclaration) {
      NameOccurrence name = Iterables.getLast(names);
      RoutineScope fromScope = name.getLocation().getScope().getEnclosingScope(RoutineScope.class);
      if (fromScope != null) {
        RoutineNameDeclaration routine = (RoutineNameDeclaration) declaration;
        Type fromType = extractType(fromScope);
        Type toType = extractType(routine);
        FileScope currentFile = fromScope.getEnclosingScope(FileScope.class);
        FileScope routineFile = routine.getScope().getEnclosingScope(FileScope.class);
        FileScope typeFile = extractFileScope(currentType);
        boolean isSameUnit = currentFile == routineFile || currentFile == typeFile;

        return isRoutineVisibleFrom(fromType, toType, routine, isSameUnit);
      }
    }
    return true;
  }

  private static Type extractType(RoutineScope scope) {
    DelphiScope routineScope = scope;
    DelphiScope typeScope = scope.getTypeScope();

    while (!(typeScope instanceof TypeScope) && routineScope instanceof RoutineScope) {
      typeScope = ((RoutineScope) routineScope).getTypeScope();
      routineScope = routineScope.getParent();
    }

    if (typeScope instanceof TypeScope) {
      return ((TypeScope) typeScope).getType();
    }

    return unknownType();
  }

  private static Type extractType(RoutineNameDeclaration routine) {
    TypeNameDeclaration typeDeclaration = routine.getTypeDeclaration();
    if (typeDeclaration == null) {
      return unknownType();
    }
    return typeDeclaration.getType();
  }

  @Nullable
  private static FileScope extractFileScope(Type type) {
    ScopedType scopedType = extractScopedType(type);
    if (scopedType != null) {
      return scopedType.typeScope().getEnclosingScope(FileScope.class);
    }
    return null;
  }

  private static boolean isRoutineVisibleFrom(
      Type fromType, Type toType, RoutineNameDeclaration routine, boolean isSameUnit) {
    if (!fromType.isUnknown() && !toType.isUnknown()) {
      if (fromType.is(toType)) {
        return true;
      } else if (fromType.isDescendantOf(toType)) {
        return isParentTypeMethodVisible(routine, isSameUnit);
      } else if (isHelperTypeAccessingExtendedType(fromType, toType)) {
        return !routine.isPrivate();
      }
    }
    return isOtherTypeMethodVisible(routine, isSameUnit);
  }

  private static boolean isHelperTypeAccessingExtendedType(Type fromType, Type toType) {
    if (fromType.isHelper()) {
      Type extendedType = ((HelperType) fromType).extendedType();
      return extendedType.is(toType) || extendedType.isDescendantOf(toType);
    }
    return false;
  }

  private static boolean isParentTypeMethodVisible(
      RoutineNameDeclaration method, boolean sameUnit) {
    return !(sameUnit ? method.isStrictPrivate() : method.isPrivate());
  }

  private static boolean isOtherTypeMethodVisible(RoutineNameDeclaration method, boolean sameUnit) {
    if (sameUnit) {
      return !method.isStrictPrivate() && !method.isStrictProtected();
    } else {
      return method.isPublic() || method.isPublished();
    }
  }

  void addName(NameOccurrenceImpl name) {
    names.add(name);
  }

  void searchForDeclaration(NameOccurrence occurrence) {
    if (currentType.isTypeParameter()) {
      searchForDeclarationInConstraintTypes(occurrence);
      return;
    }

    DelphiScope occurrenceScope = occurrence.getLocation().getScope();
    if (currentScope == null) {
      currentScope = occurrenceScope;
    }

    checkForRecordHelperScope(occurrenceScope);

    Search search = new Search(occurrence, searchMode);
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
  private void searchForDeclarationInConstraintTypes(NameOccurrence occurrence) {
    TypeParameterType type = (TypeParameterType) currentType;
    for (Type constraint : type.constraints()) {
      if (constraint instanceof ScopedType) {
        updateType(constraint);
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
      Type type = currentType;
      if (type.isClassReference()) {
        type = ((ClassReferenceType) type).classType();
      }

      HelperType helper = scope.getHelperForType(type);
      if (helper != null) {
        currentScope = helper.typeScope();
      }
    }
  }

  @Nullable
  private static ScopedType extractScopedType(Type type) {
    while (!(type instanceof ScopedType)) {
      if (type instanceof ProceduralType) {
        type = ((ProceduralType) type).returnType();
      } else if (type instanceof PointerType) {
        type = ((PointerType) type).dereferencedType();
      } else {
        break;
      }
    }

    return (type instanceof ScopedType) ? (ScopedType) type : null;
  }

  private static void registerOccurrence(NameOccurrenceImpl occurrence) {
    FileScope scope = occurrence.getLocation().getScope().getEnclosingScope(FileScope.class);
    ((FileScopeImpl) scope).registerOccurrence(occurrence.getLocation(), occurrence);
  }
}
