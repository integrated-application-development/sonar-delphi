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
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeReferenceNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.GenerifiableDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.MethodScope;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Type.ScopedType;
import org.sonar.plugins.delphi.type.Type.TypeParameterType;
import org.sonar.plugins.delphi.type.Typed;

public interface NameResolver {
  Type getApproximateType();

  Set<NameDeclaration> getDeclarations();

  List<NameDeclaration> getResolvedDeclarations();

  @Nullable
  NameDeclaration addResolvedDeclaration();

  void checkAmbiguity();

  void updateScopeAndType();

  void addToSymbolTable();

  boolean isExplicitInvocation();

  static void resolve(TypeDeclarationNode typeDeclaration) {
    typeDeclaration.getTypeNode().getParentTypeNodes().forEach(NameResolver::resolve);
    resolve(typeDeclaration.getTypeNode());
  }

  static void resolve(TypeNode type) {
    type.clearCachedType();

    List<Node> nodes = new ArrayList<>();
    nodes.add(type);

    if (!(type instanceof StructTypeNode)) {
      nodes.addAll(type.findDescendantsOfType(TypeNode.class));
      nodes.addAll(type.findChildrenOfType(PrimaryExpressionNode.class));
    }

    if (type instanceof HelperTypeNode) {
      nodes.add(((HelperTypeNode) type).getFor());
    }

    for (Node node : nodes) {
      node.findChildrenOfType(NameReferenceNode.class).forEach(NameResolver::resolve);
    }
  }

  static void resolve(NameReferenceNode reference) {
    DefaultNameResolver resolver = new DefaultNameResolver();
    resolver.readNameReference(reference);
    resolver.addToSymbolTable();
  }

  static void resolve(PrimaryExpressionNode expression) {
    DefaultNameResolver resolver = new DefaultNameResolver();
    resolver.readPrimaryExpression(expression);

    if (handleMethodReference(expression, resolver)) {
      return;
    }

    if (!resolver.isExplicitInvocation()) {
      resolver.disambiguateImplicitEmptyArgumentList();
    }

    resolver.addToSymbolTable();
  }

  static void resolve(MethodResolutionClauseNode resolutionClause) {
    DefaultNameResolver interfaceMethodResolver = new DefaultNameResolver();
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

    DefaultNameResolver concreteMethodResolver = new DefaultNameResolver();
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

  static void resolve(PropertyNode property) {
    resolve(property.getParameterListNode());

    TypeNode type = property.getTypeNode();
    if (type != null) {
      resolve(type);
    }

    PropertyReadSpecifierNode read = property.getReadSpecifier();
    if (read != null) {
      DefaultNameResolver readResolver = new DefaultNameResolver();
      readResolver.readPrimaryExpression(read.getExpression());
      readResolver.disambiguateParameters(property.getParameterTypes());
      readResolver.addToSymbolTable();
    }

    PropertyWriteSpecifierNode write = property.getWriteSpecifier();
    if (write != null) {
      List<Type> parameterTypes = new ArrayList<>(property.getParameterTypes());
      parameterTypes.add(property.getType());
      DefaultNameResolver writeResolver = new DefaultNameResolver();
      writeResolver.readPrimaryExpression(write.getExpression());
      writeResolver.disambiguateParameters(parameterTypes);
      writeResolver.addToSymbolTable();
    }
  }

  static void resolve(MethodDeclarationNode method) {
    resolveMethod(method);
  }

  static void resolve(MethodImplementationNode method) {
    DefaultNameResolver resolver = new DefaultNameResolver();
    resolver.readMethodNameInterfaceReference(method.getNameReferenceNode());

    MethodScope methodScope = method.getScope().getEnclosingScope(MethodScope.class);
    methodScope.setTypeScope(findTypeScope(resolver));
    resolveMethod(method);

    resolver.disambiguateParameters(method.getParameterTypes());
    resolver.disambiguateIsClassInvocable(method.isClassMethod());
    resolver.addToSymbolTable();

    completeTypeParameterReferences(method);
  }

  private static void resolveMethod(MethodNode method) {
    resolve(method.getMethodHeading().getMethodParametersNode());
    resolve(method.getMethodHeading().getMethodReturnType());
  }

  static void resolve(@Nullable MethodParametersNode parameters) {
    if (parameters != null) {
      resolve(parameters.getFormalParametersList());
    }
  }

  static void resolve(@Nullable MethodReturnTypeNode returnType) {
    if (returnType != null) {
      NameResolver.resolve(returnType.getTypeNode());
    }
  }

  static void resolve(@Nullable FormalParameterListNode parameterList) {
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

  static void resolve(ExpressionNode expression) {
    if (expression instanceof PrimaryExpressionNode) {
      resolve((PrimaryExpressionNode) expression);
      return;
    }
    resolveSubExpressions(expression);
  }

  static void resolveSubExpressions(ExpressionNode expression) {
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

  private static boolean handleMethodReference(
      PrimaryExpressionNode expression, DefaultNameResolver resolver) {
    Node parent = expression.jjtGetParent();
    if (parent instanceof AssignmentStatementNode) {
      Typed assignee = ((AssignmentStatementNode) parent).getAssignee();
      if (expression == assignee) {
        return false;
      }

      if (assignee.getType().isProcedural()) {
        DefaultNameResolver clone = new DefaultNameResolver(resolver);
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

  private static void completeTypeParameterReferences(MethodImplementationNode method) {
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

    if (typeParameters.size() != typeArguments.size()) {
      return;
    }

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
