package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import com.google.common.collect.Iterables;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.RaiseStatementNode;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.Qualifiable;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Typed;

public class MemoryManagementRule extends AbstractDelphiRule {

  private static final PropertyDescriptor<List<String>> MEMORY_FUNCTIONS =
      PropertyFactory.stringListProperty("memoryFunctions")
          .desc("A list of functions used for memory management")
          .defaultValue(Collections.emptyList())
          .build();

  private static final PropertyDescriptor<List<String>> WHITELISTED_NAMES =
      PropertyFactory.stringListProperty("whitelist")
          .desc("A list of constructor names which don't require memory management.")
          .defaultValue(Collections.emptyList())
          .build();

  private Set<String> memoryFunctions;
  private Set<String> whitelist;

  public MemoryManagementRule() {
    definePropertyDescriptor(MEMORY_FUNCTIONS);
    definePropertyDescriptor(WHITELISTED_NAMES);
  }

  @Override
  public void start(RuleContext data) {
    memoryFunctions = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    memoryFunctions.addAll(getProperty(MEMORY_FUNCTIONS));

    whitelist = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    whitelist.addAll(getProperty(WHITELISTED_NAMES));
  }

  @Override
  public RuleContext visit(PrimaryExpressionNode expression, RuleContext data) {
    if (shouldVisit(expression)) {
      expression.findChildrenOfType(NameReferenceNode.class).stream()
          .flatMap(reference -> reference.flatten().stream())
          .filter(MemoryManagementRule::requiresMemoryManagement)
          .map(NameReferenceNode::getIdentifier)
          .filter(identifier -> !whitelist.contains(identifier.getImage()))
          .forEach(violationNode -> addViolation(data, violationNode));
    }

    return super.visit(expression, data);
  }

  private boolean shouldVisit(PrimaryExpressionNode expression) {
    if (expression.isInheritedCall()) {
      return false;
    }

    if (isInterfaceVariableAssignment(expression)) {
      return false;
    }

    if (isInterfaceParameter(expression)) {
      return false;
    }

    if (isExceptionRaise(expression)) {
      return false;
    }

    if (isSelfCall(expression)) {
      return false;
    }

    return !isMemoryManaged(expression);
  }

  private static boolean isInterfaceVariableAssignment(PrimaryExpressionNode expression) {
    Node assignStatement = expression.findParentheses().jjtGetParent();
    if (assignStatement instanceof AssignmentStatementNode) {
      Type assignedType = ((AssignmentStatementNode) assignStatement).getAssignee().getType();
      return assignedType.isInterface();
    }
    return false;
  }

  private static boolean isInterfaceParameter(ExpressionNode expression) {
    expression = expression.findParentheses();
    Node parent = expression.jjtGetParent();

    if (!(parent instanceof ArgumentListNode)) {
      return false;
    }

    Node previous = parent.jjtGetParent().jjtGetChild(parent.jjtGetChildIndex() - 1);
    if (!(previous instanceof Typed)) {
      return false;
    }

    Type type = ((Typed) previous).getType();
    if (!type.isProcedural()) {
      return false;
    }

    if (previous instanceof NameReferenceNode) {
      NameReferenceNode nameReference = (NameReferenceNode) previous;
      if (nameReference.getLastName().getNameDeclaration() instanceof TypeNameDeclaration) {
        return false;
      }
    }

    List<ExpressionNode> arguments = ((ArgumentListNode) parent).getArguments();
    int argumentIndex = arguments.indexOf(expression);

    List<Type> parameters = ((ProceduralType) type).parameterTypes();
    Type parameterType =
        Objects.requireNonNull(Iterables.get(parameters, argumentIndex, unknownType()));

    return parameterType.isInterface();
  }

  private static boolean isExceptionRaise(PrimaryExpressionNode expression) {
    return expression.findParentheses().jjtGetParent() instanceof RaiseStatementNode;
  }

  private static boolean isSelfCall(PrimaryExpressionNode expression) {
    Node child = expression.jjtGetChild(0);
    if (child instanceof NameReferenceNode) {
      NameReferenceNode name = (NameReferenceNode) child;
      if (name.getIdentifier().getImage().equalsIgnoreCase("Self")) {
        NameReferenceNode next = name.nextName();
        return next != null && requiresMemoryManagement(next);
      }
      return requiresMemoryManagement(name);
    }
    return false;
  }

  private boolean isMemoryManaged(PrimaryExpressionNode expression) {
    Node argList = expression.findParentheses().jjtGetParent();
    if (!(argList instanceof ArgumentListNode)) {
      return false;
    }

    Node nameReference = argList.jjtGetParent().jjtGetChild(argList.jjtGetChildIndex() - 1);
    if (!(nameReference instanceof NameReferenceNode)) {
      return false;
    }

    return memoryFunctions.contains(((Qualifiable) nameReference).simpleName());
  }

  private static boolean requiresMemoryManagement(NameReferenceNode referenceNode) {
    DelphiNameOccurrence occurrence = referenceNode.getNameOccurrence();
    if (occurrence == null) {
      return false;
    }

    DelphiNameDeclaration declaration = occurrence.getNameDeclaration();
    if (!(declaration instanceof MethodNameDeclaration)) {
      return false;
    }

    return requiresMemoryManagement((MethodNameDeclaration) declaration);
  }

  private static boolean requiresMemoryManagement(MethodNameDeclaration method) {
    switch (method.getMethodKind()) {
      case CONSTRUCTOR:
        TypeNameDeclaration typeDeclaration = method.getTypeDeclaration();
        boolean isRecordConstructor =
            typeDeclaration != null && typeDeclaration.getType().isRecord();

        return !isRecordConstructor;

      case FUNCTION:
        return method.getName().equalsIgnoreCase("Clone") && returnsCovariantType(method);

      default:
        return false;
    }
  }

  private static boolean returnsCovariantType(MethodNameDeclaration method) {
    TypeNameDeclaration typeDeclaration = method.getTypeDeclaration();
    if (typeDeclaration != null) {
      Type methodType = typeDeclaration.getType();
      Type returnType = method.getReturnType();

      return methodType.is(returnType) || methodType.isSubTypeOf(returnType);
    }
    return false;
  }
}
