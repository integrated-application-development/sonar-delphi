/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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

import static org.sonar.plugins.communitydelphi.api.type.TypeFactory.unknownType;

import au.com.integradev.delphi.antlr.ast.node.TypeNodeImpl;
import au.com.integradev.delphi.symbol.NameOccurrenceImpl;
import au.com.integradev.delphi.symbol.SearchMode;
import au.com.integradev.delphi.symbol.scope.MethodScopeImpl;
import au.com.integradev.delphi.type.generic.TypeParameterTypeImpl;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayIndicesNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterListNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode;
import org.sonar.plugins.communitydelphi.api.ast.GenericArgumentsNode;
import org.sonar.plugins.communitydelphi.api.ast.HelperTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodParametersNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodResolutionClauseNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodReturnTypeNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyReadSpecifierNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyWriteSpecifierNode;
import org.sonar.plugins.communitydelphi.api.ast.RecordExpressionItemNode;
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
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.MethodScope;
import org.sonar.plugins.communitydelphi.api.type.Type;
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

  public void resolve(PrimaryExpressionNode expression) {
    NameResolver resolver = createNameResolver();
    resolver.readPrimaryExpression(expression);

    if (handleMethodReference(expression, resolver)
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
    List<MethodNameDeclaration> interfaceMethods =
        interfaceMethodResolver.getDeclarations().stream()
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

    NameResolver concreteMethodResolver = createNameResolver();
    concreteMethodResolver.readNameReference(resolutionClause.getImplementationMethodNameNode());
    List<MethodNameDeclaration> implementationMethods =
        concreteMethodResolver.getDeclarations().stream()
            .filter(MethodNameDeclaration.class::isInstance)
            .map(MethodNameDeclaration.class::cast)
            .collect(Collectors.toList());

    Set<NameDeclaration> interfaceDeclarations = interfaceMethodResolver.getDeclarations();
    Set<NameDeclaration> concreteDeclarations = concreteMethodResolver.getDeclarations();

    interfaceDeclarations.clear();
    concreteDeclarations.clear();

    for (MethodNameDeclaration interfaceCandidate : interfaceMethods) {
      boolean matched = false;

      for (MethodNameDeclaration concreteCandidate : implementationMethods) {
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
      readResolver.disambiguateParameters(property.getParameterTypes());
      readResolver.addToSymbolTable();
    }

    PropertyWriteSpecifierNode write = property.getWriteSpecifier();
    if (write != null) {
      List<Type> parameterTypes = new ArrayList<>(property.getParameterTypes());
      parameterTypes.add(property.getType());
      NameResolver writeResolver = createNameResolver();
      writeResolver.readPrimaryExpression(write.getExpression());
      writeResolver.disambiguateParameters(parameterTypes);
      writeResolver.addToSymbolTable();
    }
  }

  public void resolve(MethodDeclarationNode method) {
    resolveMethod(method);
  }

  public void resolve(MethodImplementationNode method) {
    NameResolver resolver = createNameResolver();
    resolver.readMethodNameInterfaceReference(method.getNameReferenceNode());

    MethodScope methodScope = method.getScope().getEnclosingScope(MethodScope.class);
    ((MethodScopeImpl) methodScope).setTypeScope(findTypeScope(resolver));
    resolveMethod(method);

    if (!isBareInterfaceMethodReference(method, resolver)) {
      resolver.disambiguateParameters(method.getParameterTypes());
    }

    if (method.isOperator()) {
      resolver.disambiguateReturnType(method.getReturnType());
    }

    resolver.disambiguateIsClassInvocable(method.isClassMethod());
    resolver.addToSymbolTable();

    completeTypeParameterReferences(method);
  }

  private boolean isBareInterfaceMethodReference(MethodNode method, NameResolver resolver) {
    return method.getMethodHeading().getMethodParametersNode() == null
        && resolver.getDeclarations().size() == 1;
  }

  private void resolveMethod(MethodNode method) {
    SearchMode previousSearchMode = searchMode;
    try {
      searchMode = SearchMode.METHOD_HEADING;
      resolve(method.getMethodHeading().getMethodParametersNode());
      resolve(method.getMethodHeading().getMethodReturnType());
    } finally {
      searchMode = previousSearchMode;
    }
  }

  public void resolve(@Nullable MethodParametersNode parameters) {
    if (parameters != null) {
      resolve(parameters.getFormalParametersList());
    }
  }

  public void resolve(@Nullable MethodReturnTypeNode returnType) {
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

  public MethodNameDeclaration findMethodMember(
      DelphiNode node, Type type, String name, List<Type> parameters) {
    return findInvocableMember(node, type, name, parameters, MethodNameDeclaration.class);
  }

  public PropertyNameDeclaration findPropertyMember(
      DelphiNode node, Type type, String name, List<Type> parameters) {
    return findInvocableMember(node, type, name, parameters, PropertyNameDeclaration.class);
  }

  private <T extends Invocable> T findInvocableMember(
      DelphiNode node, Type type, String name, List<Type> parameters, Class<T> declarationType) {
    NameResolver resolver = memberResolver(node, type, name);
    resolver.disambiguateParameters(parameters);
    NameDeclaration resolved = resolver.addResolvedDeclaration();
    if (declarationType.isInstance(resolved)) {
      return declarationType.cast(resolved);
    }
    return null;
  }

  private NameResolver memberResolver(DelphiNode node, Type type, String name) {
    NameOccurrenceImpl implicitOccurrence = new NameOccurrenceImpl(node, name);
    NameResolver resolver = createNameResolver();
    resolver.updateType(type);
    resolver.addName(implicitOccurrence);
    resolver.searchForDeclaration(implicitOccurrence);
    return resolver;
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
  private DelphiScope findTypeScope(NameResolver resolver) {
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

  private boolean handleAddressOf(PrimaryExpressionNode expression, NameResolver resolver) {
    Node parent = expression.getParent();

    if (parent instanceof UnaryExpressionNode) {
      UnaryExpressionNode unary = (UnaryExpressionNode) parent;
      if (unary.getOperator() == UnaryOperator.ADDRESS) {
        if (!resolver.isExplicitInvocation() && resolver.getApproximateType().isMethod()) {
          NameResolver clone = new NameResolver(resolver);
          clone.disambiguateAddressOfMethodReference();
          clone.addToSymbolTable();
        } else {
          resolver.addToSymbolTable();
        }
        return true;
      }
    }

    return false;
  }

  private boolean handleMethodReference(PrimaryExpressionNode expression, NameResolver resolver) {
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
        clone.disambiguateMethodReference((ProceduralType) assignee.getType());
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

  private boolean handlePascalReturn(PrimaryExpressionNode expression, NameResolver resolver) {
    if (expression.getChildren().size() != 1) {
      return false;
    }

    if (!resolver.getDeclarations().stream().allMatch(MethodNameDeclaration.class::isInstance)) {
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

    String methodReference = reference.getImage();
    DelphiNode node = expression;

    while ((node = node.getFirstParentOfType(MethodImplementationNode.class)) != null) {
      MethodNode method = (MethodNode) node;
      if (method.simpleName().equalsIgnoreCase(methodReference)) {
        MethodNameDeclaration methodDeclaration = method.getMethodNameDeclaration();
        resolver.getDeclarations().removeIf(declaration -> declaration != methodDeclaration);
        resolver.addToSymbolTable();
        return true;
      }
    }

    return false;
  }

  private void completeTypeParameterReferences(MethodImplementationNode method) {
    NameReferenceNode reference = method.getNameReferenceNode().getLastName();
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
        parameterDeclaration.setForwardDeclaration(occurrence.getNameDeclaration());
      }

      TypeParameterType parameterType = (TypeParameterType) parameterDeclaration.getType();
      Type argumentType = parameterReference.getType();
      if (argumentType.isTypeParameter()) {
        ((TypeParameterTypeImpl) argumentType).setFullType(parameterType);
      }
    }
  }
}
