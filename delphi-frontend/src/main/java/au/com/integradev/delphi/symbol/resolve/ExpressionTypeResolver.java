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

import static au.com.integradev.delphi.type.factory.TypeFactory.unknownType;

import au.com.integradev.delphi.operator.OperatorInvocableCollector;
import au.com.integradev.delphi.type.factory.TypeFactory;
import au.com.integradev.delphi.type.intrinsic.IntrinsicReturnType;
import au.com.integradev.delphi.type.intrinsic.IntrinsicType;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.ArrayAccessorNode;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.CommonDelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.UnaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.api.operator.Operator;
import org.sonar.plugins.communitydelphi.api.operator.UnaryOperator;
import org.sonar.plugins.communitydelphi.api.symbol.Invocable;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeParameterNameDeclaration;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ClassReferenceType;
import org.sonar.plugins.communitydelphi.api.type.Type.CollectionType;
import org.sonar.plugins.communitydelphi.api.type.Type.PointerType;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.TypeUtils;
import org.sonar.plugins.communitydelphi.api.type.Typed;

public final class ExpressionTypeResolver {
  private final TypeFactory typeFactory;

  public ExpressionTypeResolver(TypeFactory typeFactory) {
    this.typeFactory = typeFactory;
  }

  public Type resolve(BinaryExpressionNode expression) {
    ExpressionNode left = expression.getLeft();
    ExpressionNode right = expression.getRight();
    BinaryOperator operator = expression.getOperator();
    switch (operator) {
      case AS:
        return classReferenceValueType(right.getType());
      case IS:
        return typeFactory.getIntrinsic(IntrinsicType.BOOLEAN);
      default:
        return resolveOperatorType(List.of(left, right), operator);
    }
  }

  public Type resolve(UnaryExpressionNode expression) {
    ExpressionNode operand = expression.getOperand();
    UnaryOperator operator = expression.getOperator();
    if (operator == UnaryOperator.ADDRESS) {
      return typeFactory.untypedPointer();
    } else {
      return resolveOperatorType(List.of(operand), operator);
    }
  }

  public Type resolve(PrimaryExpressionNode expression) {
    Type type = unknownType();
    boolean regularArrayProperty = false;
    boolean classReference = false;

    for (int i = 0; i < expression.getChildrenCount(); ++i) {
      Node child = expression.getChild(i);
      if (child instanceof Typed) {
        type = handleTyped((Typed) child);
      } else if (child instanceof ArgumentListNode) {
        List<ExpressionNode> arguments = ((ArgumentListNode) child).getArguments();
        if (classReference) {
          type = handleHardCasts(type, arguments);
        } else {
          type = handleIntrinsicReturnTypes(type, arguments);
        }
      } else if (child instanceof ArrayAccessorNode && !regularArrayProperty) {
        type = handleArrayAccessor(type, (ArrayAccessorNode) child);
      } else if (child instanceof CommonDelphiNode) {
        type = handleSyntaxToken(type, child.getTokenType());
      }

      regularArrayProperty = isRegularArrayProperty(child);
      classReference = isClassReference(child);
    }

    return type;
  }

  private static Type classReferenceValueType(Type type) {
    if (type.isClassReference()) {
      return ((ClassReferenceType) type).classType();
    }
    return unknownType();
  }

  @Nullable
  private static NameDeclaration extractNameDeclaration(Node node) {
    if (node instanceof NameReferenceNode) {
      return ((NameReferenceNode) node).getLastName().getNameDeclaration();
    }
    return null;
  }

  private static boolean isRegularArrayProperty(Node node) {
    NameDeclaration declaration = extractNameDeclaration(node);
    return declaration instanceof PropertyNameDeclaration
        && ((PropertyNameDeclaration) declaration).isArrayProperty();
  }

  private static boolean isClassReference(Node node) {
    NameDeclaration declaration = extractNameDeclaration(node);
    if (declaration instanceof TypeNameDeclaration) {
      return true;
    }
    return node.getTokenType() == DelphiTokenType.STRING
        || node.getTokenType() == DelphiTokenType.FILE;
  }

  private Type resolveOperatorType(List<ExpressionNode> argumentNodes, Operator operator) {
    InvocationResolver resolver = new InvocationResolver();
    List<InvocationArgument> arguments =
        argumentNodes.stream()
            .map(InvocationArgument::new)
            .collect(Collectors.toUnmodifiableList());

    for (InvocationArgument argument : arguments) {
      resolver.addArgument(argument);
      createOperatorInvocables(argument.getType(), operator).stream()
          .map(InvocationCandidate::new)
          .forEach(resolver::addCandidate);
    }

    resolver.processCandidates();
    Set<InvocationCandidate> bestCandidate = resolver.chooseBest();

    if (bestCandidate.size() == 1) {
      Invocable invocable = Iterables.getLast(bestCandidate).getData();
      for (int i = 0; i < resolver.getArguments().size(); ++i) {
        InvocationArgument argument = resolver.getArguments().get(i);
        Parameter parameter = invocable.getParameter(i);
        argument.resolve(parameter.getType());
      }
      return invocable.getReturnType();
    }

    return TypeFactory.unknownType();
  }

  private Set<Invocable> createOperatorInvocables(Type type, Operator operator) {
    OperatorInvocableCollector factory = new OperatorInvocableCollector(typeFactory);
    return factory.collect(type, operator);
  }

  private static Type handleHardCasts(Type type, List<ExpressionNode> arguments) {
    if (type.isClassReference() && arguments.size() == 1) {
      return ((ClassReferenceType) type).classType();
    }
    return type;
  }

  private static Type handleIntrinsicReturnTypes(Type type, List<ExpressionNode> arguments) {
    if (type instanceof IntrinsicReturnType) {
      return ((IntrinsicReturnType) type)
          .getReturnType(
              arguments.stream()
                  .map(ExpressionNode::getType)
                  .collect(Collectors.toUnmodifiableList()));
    }
    return type;
  }

  private Type handleArrayAccessor(Type type, ArrayAccessorNode accessor) {
    type = TypeUtils.findBaseType(type);
    boolean pointerMath = type.isPointer() && ((PointerType) type).allowsPointerMath();
    type = TypeUtils.dereference(type);
    type = TypeUtils.findBaseType(type);

    for (int i = 0; i < accessor.getExpressions().size(); ++i) {
      if (accessor.getImplicitNameOccurrence() != null) {
        type = handleNameOccurrence(accessor.getImplicitNameOccurrence());
      } else if (type.isArray()) {
        type = ((CollectionType) type).elementType();
      } else if (TypeUtils.isNarrowString(type)) {
        type = typeFactory.getIntrinsic(IntrinsicType.ANSICHAR);
      } else if (TypeUtils.isWideString(type)) {
        type = typeFactory.getIntrinsic(IntrinsicType.WIDECHAR);
      } else if (pointerMath) {
        pointerMath = false;
      } else {
        type = unknownType();
      }
    }

    return type;
  }

  private Type handleTyped(Typed typed) {
    if (typed instanceof NameReferenceNode) {
      return handleNameReference((NameReferenceNode) typed);
    }
    return typed.getType();
  }

  private Type handleNameReference(NameReferenceNode reference) {
    Type type = null;

    for (NameReferenceNode name : reference.flatten()) {
      NameOccurrence occurrence = name.getNameOccurrence();
      if (occurrence == null) {
        continue;
      }

      if (isConstructor(occurrence)) {
        if (type == null) {
          type = findCurrentType(reference);
        }

        if (type.isClassReference()) {
          type = ((ClassReferenceType) type).classType();
        }
      } else {
        type = handleNameOccurrence(occurrence);
      }
    }

    if (type == null) {
      type = unknownType();
    }

    return type;
  }

  private static Type findCurrentType(DelphiNode node) {
    MethodNode method = node.getFirstParentOfType(MethodNode.class);
    if (method != null) {
      TypeNameDeclaration typeDeclaration = method.getTypeDeclaration();
      if (typeDeclaration != null) {
        return typeDeclaration.getType();
      }
    }
    return unknownType();
  }

  private static boolean isConstructor(NameOccurrence occurrence) {
    NameDeclaration declaration = occurrence.getNameDeclaration();
    return declaration instanceof MethodNameDeclaration
        && ((MethodNameDeclaration) declaration).getMethodKind() == MethodKind.CONSTRUCTOR;
  }

  private Type handleNameOccurrence(NameOccurrence occurrence) {
    NameDeclaration declaration = occurrence.getNameDeclaration();

    if (declaration instanceof Typed) {
      Type type = ((Typed) declaration).getType();

      if (declaration instanceof TypeNameDeclaration
          || declaration instanceof TypeParameterNameDeclaration) {
        type = typeFactory.classOf(null, type);
      }

      if (type.isProcedural() && occurrence.isExplicitInvocation()) {
        type = ((ProceduralType) type).returnType();
      }

      return type;
    }

    return unknownType();
  }

  private Type handleSyntaxToken(Type type, DelphiTokenType tokenType) {
    switch (tokenType) {
      case DEREFERENCE:
      case DOT:
        // Delphi Extended syntax is assumed.
        // See: http://docwiki.embarcadero.com/RADStudio/en/Extended_syntax_(Delphi)
        return TypeUtils.dereference(type);

      case STRING:
        return typeFactory.classOf(null, typeFactory.getIntrinsic(IntrinsicType.UNICODESTRING));

      case FILE:
        return typeFactory.classOf(null, typeFactory.untypedFile());

      default:
        // Do nothing
    }
    return unknownType();
  }
}
