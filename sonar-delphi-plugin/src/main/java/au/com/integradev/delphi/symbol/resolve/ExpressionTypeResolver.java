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

import static au.com.integradev.delphi.type.DelphiType.unknownType;
import static com.google.common.collect.Iterables.getLast;

import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.ast.node.ArgumentListNode;
import au.com.integradev.delphi.antlr.ast.node.ArrayAccessorNode;
import au.com.integradev.delphi.antlr.ast.node.BinaryExpressionNode;
import au.com.integradev.delphi.antlr.ast.node.CommonDelphiNode;
import au.com.integradev.delphi.antlr.ast.node.DelphiNode;
import au.com.integradev.delphi.antlr.ast.node.ExpressionNode;
import au.com.integradev.delphi.antlr.ast.node.MethodNode;
import au.com.integradev.delphi.antlr.ast.node.NameReferenceNode;
import au.com.integradev.delphi.antlr.ast.node.PrimaryExpressionNode;
import au.com.integradev.delphi.antlr.ast.node.UnaryExpressionNode;
import au.com.integradev.delphi.operator.BinaryOperator;
import au.com.integradev.delphi.operator.Operator;
import au.com.integradev.delphi.operator.OperatorInvocableCollector;
import au.com.integradev.delphi.operator.UnaryOperator;
import au.com.integradev.delphi.symbol.DelphiNameOccurrence;
import au.com.integradev.delphi.symbol.declaration.DelphiNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.MethodKind;
import au.com.integradev.delphi.symbol.declaration.MethodNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.PropertyNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.TypeNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.TypeParameterNameDeclaration;
import au.com.integradev.delphi.type.DelphiType;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Type.ClassReferenceType;
import au.com.integradev.delphi.type.Type.CollectionType;
import au.com.integradev.delphi.type.Type.PointerType;
import au.com.integradev.delphi.type.Type.ProceduralType;
import au.com.integradev.delphi.type.TypeUtils;
import au.com.integradev.delphi.type.Typed;
import au.com.integradev.delphi.type.factory.TypeFactory;
import au.com.integradev.delphi.type.intrinsic.IntrinsicReturnType;
import au.com.integradev.delphi.type.intrinsic.IntrinsicType;
import au.com.integradev.delphi.type.parameter.Parameter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.ast.Node;

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

    for (int i = 0; i < expression.jjtGetNumChildren(); ++i) {
      Node child = expression.jjtGetChild(i);
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
        type = handleSyntaxToken(type, child.jjtGetId());
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
  private static DelphiNameDeclaration extractNameDeclaration(Node node) {
    if (node instanceof NameReferenceNode) {
      return ((NameReferenceNode) node).getLastName().getNameDeclaration();
    }
    return null;
  }

  private static boolean isRegularArrayProperty(Node node) {
    DelphiNameDeclaration declaration = extractNameDeclaration(node);
    return declaration instanceof PropertyNameDeclaration
        && ((PropertyNameDeclaration) declaration).isArrayProperty();
  }

  private static boolean isClassReference(Node node) {
    DelphiNameDeclaration declaration = extractNameDeclaration(node);
    if (declaration instanceof TypeNameDeclaration) {
      return true;
    }
    return node.jjtGetId() == DelphiLexer.STRING || node.jjtGetId() == DelphiLexer.FILE;
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
      Invocable invocable = getLast(bestCandidate).getData();
      for (int i = 0; i < resolver.getArguments().size(); ++i) {
        InvocationArgument argument = resolver.getArguments().get(i);
        Parameter parameter = invocable.getParameter(i);
        argument.resolve(parameter.getType());
      }
      return invocable.getReturnType();
    }

    return DelphiType.unknownType();
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
      DelphiNameOccurrence occurrence = name.getNameOccurrence();
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

  private static boolean isConstructor(DelphiNameOccurrence occurrence) {
    DelphiNameDeclaration declaration = occurrence.getNameDeclaration();
    return declaration instanceof MethodNameDeclaration
        && ((MethodNameDeclaration) declaration).getMethodKind() == MethodKind.CONSTRUCTOR;
  }

  private Type handleNameOccurrence(DelphiNameOccurrence occurrence) {
    DelphiNameDeclaration declaration = occurrence.getNameDeclaration();

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

  private Type handleSyntaxToken(Type type, int id) {
    switch (id) {
      case DelphiLexer.POINTER:
      case DelphiLexer.DOT:
        // Delphi Extended syntax is assumed.
        // See: http://docwiki.embarcadero.com/RADStudio/en/Extended_syntax_(Delphi)
        return TypeUtils.dereference(type);

      case DelphiLexer.STRING:
        return typeFactory.classOf(null, typeFactory.getIntrinsic(IntrinsicType.UNICODESTRING));

      case DelphiLexer.FILE:
        return typeFactory.classOf(null, typeFactory.untypedFile());

      default:
        // Do nothing
    }
    return unknownType();
  }
}
