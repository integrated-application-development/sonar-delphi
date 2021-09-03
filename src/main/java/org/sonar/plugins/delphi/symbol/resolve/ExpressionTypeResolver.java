package org.sonar.plugins.delphi.symbol.resolve;

import static com.google.common.collect.Iterables.getLast;
import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.ArrayAccessorNode;
import org.sonar.plugins.delphi.antlr.ast.node.BinaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.CommonDelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnaryExpressionNode;
import org.sonar.plugins.delphi.operator.BinaryOperator;
import org.sonar.plugins.delphi.operator.Operator;
import org.sonar.plugins.delphi.operator.OperatorInvocableCollector;
import org.sonar.plugins.delphi.operator.UnaryOperator;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeParameterNameDeclaration;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ClassReferenceType;
import org.sonar.plugins.delphi.type.Type.CollectionType;
import org.sonar.plugins.delphi.type.Type.PointerType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.TypeUtils;
import org.sonar.plugins.delphi.type.Typed;
import org.sonar.plugins.delphi.type.factory.TypeFactory;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicReturnType;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;
import org.sonar.plugins.delphi.type.parameter.Parameter;

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
        type = typeFactory.classOf(type);
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
        return typeFactory.classOf(typeFactory.getIntrinsic(IntrinsicType.UNICODESTRING));

      case DelphiLexer.FILE:
        return typeFactory.classOf(typeFactory.untypedFile());

      default:
        // Do nothing
    }
    return unknownType();
  }
}
