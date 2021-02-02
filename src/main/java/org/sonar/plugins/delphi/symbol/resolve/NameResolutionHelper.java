package org.sonar.plugins.delphi.symbol.resolve;

import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.node.AnonymousMethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterListNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode;
import org.sonar.plugins.delphi.antlr.ast.node.GenericArgumentsNode;
import org.sonar.plugins.delphi.antlr.ast.node.HelperTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodParametersNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodResolutionClauseNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodReturnTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyReadSpecifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyWriteSpecifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.RecordExpressionItemNode;
import org.sonar.plugins.delphi.antlr.ast.node.StructTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.SubRangeTypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeReferenceNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.GenerifiableDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.MethodScope;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.ScopedType;
import org.sonar.plugins.delphi.type.Type.TypeParameterType;
import org.sonar.plugins.delphi.type.Typed;
import org.sonar.plugins.delphi.type.factory.TypeFactory;

public class NameResolutionHelper {
  private final TypeFactory typeFactory;

  public NameResolutionHelper(TypeFactory typeFactory) {
    this.typeFactory = typeFactory;
  }

  private NameResolver createNameResolver() {
    return new NameResolver(typeFactory);
  }

  public void resolve(TypeDeclarationNode typeDeclaration) {
    typeDeclaration.getTypeNode().getParentTypeNodes().forEach(this::resolve);
    resolve(typeDeclaration.getTypeNode());
  }

  public void resolve(TypeNode type) {
    type.clearCachedType();

    List<Node> nodes = new ArrayList<>();
    nodes.add(type);

    if (type instanceof SubRangeTypeNode) {
      SubRangeTypeNode subrange = (SubRangeTypeNode) type;
      resolve(subrange.getLowExpression());
      resolve(subrange.getHighExpression());
      return;
    }

    if (!(type instanceof StructTypeNode)) {
      nodes.addAll(type.findDescendantsOfType(TypeNode.class));
      nodes.addAll(type.findChildrenOfType(PrimaryExpressionNode.class));
    }

    if (type instanceof HelperTypeNode) {
      nodes.add(((HelperTypeNode) type).getFor());
    }

    for (Node node : nodes) {
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

    if (handleMethodReference(expression, resolver) || handlePascalReturn(expression, resolver)) {
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
    methodScope.setTypeScope(findTypeScope(resolver));
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
    resolve(method.getMethodHeading().getMethodParametersNode());
    resolve(method.getMethodHeading().getMethodReturnType());
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
    DelphiNameOccurrence implicitOccurrence = new DelphiNameOccurrence(node, name);
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

  private boolean handleMethodReference(PrimaryExpressionNode expression, NameResolver resolver) {
    Node parent = expression.jjtGetParent();
    if (parent instanceof AssignmentStatementNode) {
      Typed assignee = ((AssignmentStatementNode) parent).getAssignee();
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
    if (expression.jjtGetNumChildren() != 1) {
      return false;
    }

    if (!resolver.getDeclarations().stream().allMatch(MethodNameDeclaration.class::isInstance)) {
      return false;
    }

    Node parent = expression.jjtGetParent();
    if (!(parent instanceof AssignmentStatementNode)) {
      return false;
    }

    ExpressionNode assignee = ((AssignmentStatementNode) parent).getAssignee();
    if (expression != assignee) {
      return false;
    }

    Node child = expression.jjtGetChild(0);
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

      DelphiNameOccurrence occurrence = parameterReference.getNameNode().getNameOccurrence();
      if (occurrence != null) {
        parameterDeclaration.setForwardDeclaration(occurrence.getNameDeclaration());
      }

      TypeParameterType parameterType = (TypeParameterType) parameterDeclaration.getType();
      Type argumentType = parameterReference.getType();
      if (argumentType.isTypeParameter()) {
        ((TypeParameterType) argumentType).setFullType(parameterType);
      }
    }
  }
}
