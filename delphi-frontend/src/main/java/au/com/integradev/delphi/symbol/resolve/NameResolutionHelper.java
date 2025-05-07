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

import static java.util.Collections.emptyList;
import static org.sonar.plugins.communitydelphi.api.type.TypeFactory.unknownType;

import au.com.integradev.delphi.antlr.ast.node.ForInStatementNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.TypeNodeImpl;
import au.com.integradev.delphi.symbol.SearchMode;
import au.com.integradev.delphi.symbol.SymbolicNode;
import au.com.integradev.delphi.symbol.declaration.NameDeclarationImpl;
import au.com.integradev.delphi.symbol.occurrence.EnumeratorOccurrenceImpl;
import au.com.integradev.delphi.symbol.occurrence.NameOccurrenceImpl;
import au.com.integradev.delphi.symbol.scope.FileScopeImpl;
import au.com.integradev.delphi.symbol.scope.RoutineScopeImpl;
import au.com.integradev.delphi.type.generic.TypeParameterTypeImpl;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayIndicesNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.AttributeListNode;
import org.sonar.plugins.communitydelphi.api.ast.AttributeNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ForInStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterListNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode;
import org.sonar.plugins.communitydelphi.api.ast.GenericArgumentsNode;
import org.sonar.plugins.communitydelphi.api.ast.HelperTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodResolutionClauseNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyImplementsSpecifierNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyReadSpecifierNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyStoredSpecifierNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyWriteSpecifierNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordExpressionItemNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineParametersNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineReturnTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.StructTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.SubRangeTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.UnaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.operator.UnaryOperator;
import org.sonar.plugins.communitydelphi.api.symbol.Invocable;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.GenerifiableDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.RoutineScope;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.AliasType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;
import org.sonar.plugins.communitydelphi.api.type.Type.TypeParameterType;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public class NameResolutionHelper {
  private final TypeFactory typeFactory;
  private SearchMode searchMode;

  public NameResolutionHelper(TypeFactory typeFactory) {
    this(typeFactory, SearchMode.DEFAULT);
  }

  public NameResolutionHelper(TypeFactory typeFactory, SearchMode searchMode) {
    this.typeFactory = typeFactory;
    this.searchMode = searchMode;
  }

  private NameResolver createNameResolver() {
    return new NameResolver(typeFactory, searchMode);
  }

  public void resolve(TypeDeclarationNode typeDeclaration) {
    AttributeListNode attributeListNode = typeDeclaration.getAttributeList();
    if (attributeListNode != null) {
      attributeListNode.getAttributes().forEach(this::resolve);
    }

    typeDeclaration.getTypeNode().getParentTypeNodes().forEach(this::resolve);
    resolve(typeDeclaration.getTypeNode());
  }

  public void resolve(TypeNode type) {
    ((TypeNodeImpl) type).clearCachedType();

    List<DelphiNode> nodes = new ArrayList<>();
    nodes.add(type);

    if (type instanceof SubRangeTypeNode) {
      SubRangeTypeNode subrange = (SubRangeTypeNode) type;
      resolve(subrange.getLowExpression());
      resolve(subrange.getHighExpression());
      return;
    }

    if (type instanceof ArrayTypeNode) {
      ArrayTypeNode array = (ArrayTypeNode) type;
      resolve(array.getElementTypeNode());
      ArrayIndicesNode arrayIndices = array.getArrayIndices();
      if (arrayIndices != null) {
        arrayIndices.getTypeNodes().forEach(this::resolve);
      }
      return;
    }

    if (!(type instanceof StructTypeNode)) {
      nodes.addAll(type.findDescendantsOfType(TypeNode.class));
      nodes.addAll(type.findChildrenOfType(PrimaryExpressionNode.class));
    }

    if (type instanceof HelperTypeNode) {
      nodes.add(((HelperTypeNode) type).getFor());
    }

    for (DelphiNode node : nodes) {
      node.findChildrenOfType(NameReferenceNode.class).forEach(this::resolve);
    }
  }

  public void resolve(NameReferenceNode reference) {
    NameResolver resolver = createNameResolver();
    resolver.readNameReference(reference);
    resolver.addToSymbolTable();
  }

  public void resolve(AttributeNode attribute) {
    NameResolver resolver = createNameResolver();
    resolver.readAttribute(attribute);
    resolver.addToSymbolTable();
  }

  public void resolve(PrimaryExpressionNode expression) {
    NameResolver resolver = createNameResolver();
    resolver.readPrimaryExpression(expression);

    if (handleRoutineReference(expression, resolver)
        || handleAddressOf(expression, resolver)
        || handlePascalReturn(expression, resolver)) {
      return;
    }

    if (!resolver.isExplicitInvocation()) {
      resolver.disambiguateImplicitEmptyArgumentList();
    }

    resolver.addToSymbolTable();
  }

  public void resolve(MethodResolutionClauseNode resolutionClause) {
    NameResolver interfaceMethodResolver = createNameResolver();
    interfaceMethodResolver.readNameReference(resolutionClause.getInterfaceMethodNameNode());
    List<RoutineNameDeclaration> interfaceMethods =
        interfaceMethodResolver.getDeclarations().stream()
            .filter(RoutineNameDeclaration.class::isInstance)
            .map(RoutineNameDeclaration.class::cast)
            .filter(method -> method.getTypeDeclaration() != null)
            .sorted(
                (left, right) -> {
                  Type leftType = left.getTypeDeclaration().getType();
                  TypeNameDeclaration typeDecl = right.getTypeDeclaration();
                  Type rightType = (typeDecl == null) ? unknownType() : typeDecl.getType();

                  if (leftType.is(rightType)) {
                    return left.getNode().getBeginLine() - right.getNode().getBeginLine();
                  } else if (leftType.isDescendantOf(rightType)) {
                    return 1;
                  } else {
                    return -1;
                  }
                })
            .collect(Collectors.toList());

    NameResolver concreteMethodResolver = createNameResolver();
    concreteMethodResolver.readNameReference(resolutionClause.getImplementationMethodNameNode());
    List<RoutineNameDeclaration> implementationMethods =
        concreteMethodResolver.getDeclarations().stream()
            .filter(RoutineNameDeclaration.class::isInstance)
            .map(RoutineNameDeclaration.class::cast)
            .collect(Collectors.toList());

    Set<NameDeclaration> interfaceDeclarations = interfaceMethodResolver.getDeclarations();
    Set<NameDeclaration> concreteDeclarations = concreteMethodResolver.getDeclarations();

    interfaceDeclarations.clear();
    concreteDeclarations.clear();

    for (RoutineNameDeclaration interfaceCandidate : interfaceMethods) {
      boolean matched = false;

      for (RoutineNameDeclaration concreteCandidate : implementationMethods) {
        if (interfaceCandidate.getParameters().equals(concreteCandidate.getParameters())) {
          interfaceDeclarations.add(interfaceCandidate);
          concreteDeclarations.add(concreteCandidate);
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

  public void resolve(PropertyNode property) {
    resolve(property.getParameterListNode());

    TypeNode type = property.getTypeNode();
    if (type != null) {
      resolve(type);
    }

    PropertyReadSpecifierNode read = property.getReadSpecifier();
    if (read != null) {
      NameResolver readResolver = createNameResolver();
      readResolver.readPrimaryExpression(read.getExpression());
      if (isResolvingRoutine(readResolver)) {
        readResolver.disambiguateParameters(getGetterParameterTypes(property));
      }
      readResolver.addToSymbolTable();
    }

    PropertyWriteSpecifierNode write = property.getWriteSpecifier();
    if (write != null) {
      NameResolver writeResolver = createNameResolver();
      writeResolver.readPrimaryExpression(write.getExpression());
      if (isResolvingRoutine(writeResolver)) {
        writeResolver.disambiguateParameters(getSetterParameterTypes(property));
      }
      writeResolver.addToSymbolTable();
    }

    PropertyImplementsSpecifierNode impl = property.getImplementsSpecifier();
    if (impl != null) {
      impl.getTypeReferences().stream().map(TypeReferenceNode::getNameNode).forEach(this::resolve);
    }

    PropertyStoredSpecifierNode stored = property.getStoredSpecifier();
    if (stored != null && stored.getExpression() instanceof PrimaryExpressionNode) {
      NameResolver storedResolver = createNameResolver();
      storedResolver.readPrimaryExpression((PrimaryExpressionNode) stored.getExpression());
      if (isResolvingRoutine(storedResolver)) {
        storedResolver.disambiguateParameters(getStorageParameterTypes(property));
      }
      storedResolver.addToSymbolTable();
    }
  }

  private static boolean isResolvingRoutine(NameResolver resolver) {
    return resolver.getDeclarations().stream().anyMatch(RoutineNameDeclaration.class::isInstance);
  }

  private List<Type> getGetterParameterTypes(PropertyNode property) {
    if (property.getIndexSpecifier() == null) {
      return property.getParameterTypes();
    }

    var parameterTypes = new ArrayList<>(property.getParameterTypes());
    parameterTypes.add(typeFactory.getIntrinsic(IntrinsicType.INTEGER));
    return parameterTypes;
  }

  private List<Type> getSetterParameterTypes(PropertyNode property) {
    var parameterTypes = new ArrayList<>(property.getParameterTypes());
    if (property.getIndexSpecifier() != null) {
      parameterTypes.add(typeFactory.getIntrinsic(IntrinsicType.INTEGER));
    }
    parameterTypes.add(property.getType());
    return parameterTypes;
  }

  private List<Type> getStorageParameterTypes(PropertyNode property) {
    if (property.getIndexSpecifier() != null) {
      return List.of(typeFactory.getIntrinsic(IntrinsicType.INTEGER));
    } else {
      return Collections.emptyList();
    }
  }

  public void resolve(RoutineDeclarationNode routine) {
    resolveRoutine(routine);
  }

  public void resolve(RoutineImplementationNode routine) {
    NameResolver resolver = createNameResolver();
    resolver.readRoutineNameInterfaceReference(routine.getNameReferenceNode());

    RoutineScope routineScope = routine.getScope().getEnclosingScope(RoutineScope.class);
    ((RoutineScopeImpl) routineScope).setTypeScope(findTypeScope(resolver));
    resolveRoutine(routine);

    if (!isBareInterfaceRoutineReference(routine, resolver)) {
      resolver.disambiguateParameters(routine.getParameterTypes());
    }

    if (routine.isOperator()) {
      resolver.disambiguateReturnType(routine.getReturnType());
    }

    resolver.disambiguateIsClassInvocable(routine.isClassMethod());

    if (resolver.nameResolutionFailed() && !routine.getNameReferenceNode().isQualified()) {
      // No interface declaration found, and not a qualified name so it can't be a method.
      // It must be an implementation-local routine.
      return;
    }

    resolver.addToSymbolTable();

    completeTypeParameterReferences(routine);
  }

  public void resolve(ForInStatementNode forStatement) {
    DelphiScope scope = forStatement.getScope();
    Type enumerableType = forStatement.getEnumerable().getType();

    NameOccurrenceImpl getEnumerator = imaginaryOccurrence("GetEnumerator", scope);
    if (!resolveMethod(getEnumerator, enumerableType, emptyList())) {
      return;
    }

    var getEnumeratorDeclaration = (RoutineNameDeclaration) getEnumerator.getNameDeclaration();
    Type enumeratorType = getEnumeratorDeclaration.getReturnType();

    NameOccurrenceImpl moveNext = imaginaryOccurrence("MoveNext", scope);
    NameOccurrenceImpl current = imaginaryOccurrence("Current", scope);

    boolean resolved = resolveMethod(moveNext, enumeratorType, emptyList());
    resolved = resolveProperty(current, enumeratorType, emptyList()) && resolved;

    if (resolved) {
      var enumeratorOccurrence = new EnumeratorOccurrenceImpl(getEnumerator, moveNext, current);
      var fileScope = forStatement.getScope().getEnclosingScope(FileScope.class);
      ((FileScopeImpl) fileScope).registerOccurrence(forStatement, enumeratorOccurrence);
      ((ForInStatementNodeImpl) forStatement).setEnumeratorOccurrence(enumeratorOccurrence);
    }
  }

  private boolean resolveMethod(NameOccurrenceImpl occurrence, Type type, List<Type> parameters) {
    return resolveInvocableMember(occurrence, type, parameters, RoutineNameDeclaration.class);
  }

  private boolean resolveProperty(NameOccurrenceImpl occurrence, Type type, List<Type> parameters) {
    return resolveInvocableMember(occurrence, type, parameters, PropertyNameDeclaration.class);
  }

  private boolean resolveInvocableMember(
      NameOccurrenceImpl occurrence,
      Type type,
      List<Type> parameters,
      Class<? extends Invocable> declarationClass) {
    NameResolver resolver = createNameResolver();
    resolver.updateType(type);
    resolver.addName(occurrence);
    resolver.searchForDeclaration(occurrence);
    resolver.disambiguateParameters(parameters);

    NameDeclaration resolved = resolver.addResolvedDeclaration();
    if (declarationClass.isInstance(resolved)) {
      resolver.addToSymbolTable();
      return true;
    }

    return false;
  }

  private static NameOccurrenceImpl imaginaryOccurrence(String image, DelphiScope scope) {
    return new NameOccurrenceImpl(SymbolicNode.imaginary(image, scope));
  }

  private static boolean isBareInterfaceRoutineReference(
      RoutineNode routine, NameResolver resolver) {
    return routine.getRoutineHeading().getRoutineParametersNode() == null
        && resolver.getDeclarations().size() == 1;
  }

  private void resolveRoutine(RoutineNode routine) {
    SearchMode previousSearchMode = searchMode;
    try {
      searchMode = SearchMode.ROUTINE_HEADING;
      resolve(routine.getRoutineHeading().getRoutineParametersNode());
      resolve(routine.getRoutineHeading().getRoutineReturnType());
    } finally {
      searchMode = previousSearchMode;
    }
  }

  public void resolve(@Nullable RoutineParametersNode parameters) {
    if (parameters != null) {
      resolve(parameters.getFormalParametersList());
    }
  }

  public void resolve(@Nullable RoutineReturnTypeNode returnType) {
    if (returnType != null) {
      resolve(returnType.getTypeNode());
    }
  }

  public void resolve(@Nullable FormalParameterListNode parameterList) {
    if (parameterList != null) {
      parameterList
          .findChildrenOfType(FormalParameterNode.class)
          .forEach(
              parameter -> {
                TypeNode type = parameter.getTypeNode();
                if (type != null) {
                  resolve(type);
                }
              });
    }
  }

  public void resolve(ExpressionNode expression) {
    if (expression instanceof PrimaryExpressionNode) {
      resolve((PrimaryExpressionNode) expression);
      return;
    }
    resolveSubExpressions(expression);
  }

  public void resolveSubExpressions(ExpressionNode expression) {
    if (expression instanceof AnonymousMethodNode) {
      return;
    }

    for (var descendant : expression.findDescendantsOfType(PrimaryExpressionNode.class)) {
      resolve(descendant);
    }
  }

  @Nullable
  private static DelphiScope findTypeScope(NameResolver resolver) {
    DelphiScope typeScope = null;
    for (NameDeclaration declaration : resolver.getResolvedDeclarations()) {
      if (declaration instanceof TypeNameDeclaration) {
        Type type = ((TypeNameDeclaration) declaration).getType();
        if (type instanceof ScopedType) {
          typeScope = ((ScopedType) type).typeScope();
        }
      }
    }
    return typeScope;
  }

  private static boolean handleAddressOf(PrimaryExpressionNode expression, NameResolver resolver) {
    Node parent = expression.getParent();

    if (parent instanceof UnaryExpressionNode) {
      UnaryExpressionNode unary = (UnaryExpressionNode) parent;
      if (unary.getOperator() == UnaryOperator.ADDRESS) {
        if (!resolver.isExplicitInvocation() && resolver.getApproximateType().isRoutine()) {
          NameResolver clone = new NameResolver(resolver);
          clone.disambiguateAddressOfRoutineReference();
          clone.addToSymbolTable();
        } else {
          resolver.addToSymbolTable();
        }
        return true;
      }
    }

    return false;
  }

  private static boolean handleRoutineReference(
      PrimaryExpressionNode expression, NameResolver resolver) {
    DelphiNode parent = expression.getParent();

    if (parent instanceof UnaryExpressionNode) {
      UnaryExpressionNode unary = (UnaryExpressionNode) parent;
      if (unary.getOperator() == UnaryOperator.ADDRESS) {
        parent = parent.getParent();
      }
    }

    if (parent instanceof AssignmentStatementNode) {
      ExpressionNode assignee = ((AssignmentStatementNode) parent).getAssignee();
      if (expression == assignee) {
        return false;
      }

      if (assignee.getType().isProcedural()) {
        NameResolver clone = new NameResolver(resolver);
        clone.disambiguateRoutineReference((ProceduralType) assignee.getType());
        if (!clone.getDeclarations().isEmpty()) {
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

  private static boolean handlePascalReturn(
      PrimaryExpressionNode expression, NameResolver resolver) {
    if (expression.getChildren().size() != 1) {
      return false;
    }

    if (!resolver.getDeclarations().stream().allMatch(RoutineNameDeclaration.class::isInstance)) {
      return false;
    }

    Node parent = expression.getParent();
    if (!(parent instanceof AssignmentStatementNode)) {
      return false;
    }

    ExpressionNode assignee = ((AssignmentStatementNode) parent).getAssignee();
    if (expression != assignee) {
      return false;
    }

    Node child = expression.getChild(0);
    if (!(child instanceof NameReferenceNode)) {
      return false;
    }

    NameReferenceNode reference = (NameReferenceNode) child;
    if (reference.nextName() != null) {
      return false;
    }

    String routineReference = reference.getImage();
    DelphiNode node = expression;

    while ((node = node.getFirstParentOfType(RoutineImplementationNode.class)) != null) {
      RoutineNode routine = (RoutineNode) node;
      if (routine.simpleName().equalsIgnoreCase(routineReference)) {
        RoutineNameDeclaration routineDeclaration = routine.getRoutineNameDeclaration();
        resolver.getDeclarations().removeIf(declaration -> declaration != routineDeclaration);
        resolver.addToSymbolTable();
        return true;
      }
    }

    return false;
  }

  private static void completeTypeParameterReferences(RoutineImplementationNode routine) {
    NameReferenceNode reference = routine.getNameReferenceNode().getLastName();
    NameDeclaration declaration = reference.getNameDeclaration();
    if (!(declaration instanceof GenerifiableDeclaration)) {
      return;
    }

    GenericArgumentsNode genericArguments = reference.getGenericArguments();
    if (genericArguments == null) {
      return;
    }

    GenerifiableDeclaration generifiable = (GenerifiableDeclaration) declaration;
    List<TypedDeclaration> typeParameters = generifiable.getTypeParameters();
    List<TypeReferenceNode> typeArguments =
        genericArguments.findChildrenOfType(TypeReferenceNode.class);

    for (int i = 0; i < typeParameters.size(); ++i) {
      TypedDeclaration parameterDeclaration = typeParameters.get(i);
      TypeReferenceNode parameterReference = typeArguments.get(i);

      NameOccurrence occurrence = parameterReference.getNameNode().getNameOccurrence();
      if (occurrence != null) {
        ((NameDeclarationImpl) parameterDeclaration)
            .setForwardDeclaration(occurrence.getNameDeclaration());
      }

      TypeParameterType parameterType = (TypeParameterType) parameterDeclaration.getType();
      Type argumentType = parameterReference.getType();

      while (argumentType.isWeakAlias()) {
        argumentType = ((AliasType) argumentType).aliasedType();
      }

      if (!argumentType.isAlias() && argumentType.isTypeParameter()) {
        ((TypeParameterTypeImpl) argumentType).setFullType(parameterType);
      }
    }
  }
}
