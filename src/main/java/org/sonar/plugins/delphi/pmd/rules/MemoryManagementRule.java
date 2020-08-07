package org.sonar.plugins.delphi.pmd.rules;

import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.RaiseStatementNode;
import org.sonar.plugins.delphi.symbol.Qualifiable;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ProceduralType;
import org.sonar.plugins.delphi.type.Typed;

public class MemoryManagementRule extends AbstractDelphiRule {

  private static final PropertyDescriptor<List<String>> MEMORY_FUNCTIONS =
      PropertyFactory.stringListProperty("memoryFunctions")
          .desc("A list of functions used for memory management")
          .emptyDefaultValue()
          .build();

  private static final PropertyDescriptor<List<String>> WHITELISTED_NAMES =
      PropertyFactory.stringListProperty("whitelist")
          .desc("A list of constructor names which don't require memory management.")
          .emptyDefaultValue()
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

  private static boolean requiresMemoryManagement(NameReferenceNode reference) {
    DelphiNameDeclaration declaration = reference.getNameDeclaration();
    if (declaration instanceof MethodNameDeclaration) {
      MethodNameDeclaration method = (MethodNameDeclaration) declaration;
      MethodKind kind = method.getMethodKind();

      if (kind == MethodKind.CONSTRUCTOR) {
        NameReferenceNode previous = reference.prevName();
        return previous != null
            && !isExplicitSelf(previous)
            && !isObjectInstance(previous)
            && !isRecordConstructor(method);
      }

      if (kind == MethodKind.FUNCTION) {
        return method.getName().equalsIgnoreCase("Clone") && returnsCovariantType(method);
      }
    }
    return false;
  }

  private static boolean isExplicitSelf(NameReferenceNode reference) {
    return reference.getNameOccurrence() != null && reference.getNameOccurrence().isSelf();
  }

  private static boolean isObjectInstance(NameReferenceNode reference) {
    NameDeclaration declaration = reference.getNameDeclaration();
    return declaration instanceof VariableNameDeclaration
        && !((Typed) declaration).getType().isClassReference();
  }

  private static boolean isRecordConstructor(MethodNameDeclaration method) {
    TypeNameDeclaration typeDeclaration = method.getTypeDeclaration();
    return typeDeclaration != null && typeDeclaration.getType().isRecord();
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
