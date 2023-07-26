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
package au.com.integradev.delphi.checks;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Set;
import org.sonar.check.Rule;
import org.sonar.check.RuleProperty;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.BinaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.RaiseStatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.operator.BinaryOperator;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.Typed;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "MemoryManagementRule", repositoryKey = "delph")
@Rule(key = "MemoryManagement")
public class MemoryManagementCheck extends DelphiCheck {
  private static final String MESSAGE = "Use memory management to manage this object's lifetime.";

  @RuleProperty(
      key = "memoryFunctions",
      description =
          "Comma-delimited list of fully-qualified method names used for memory management")
  public String memoryFunctions;

  @RuleProperty(
      key = "whitelist",
      description =
          "Comma-delimited list of constructor names which don't require memory management.")
  public String whitelist;

  private Set<String> memoryFunctionsSet;
  private Set<String> whitelistSet;

  @Override
  public void start(DelphiCheckContext context) {
    memoryFunctionsSet =
        ImmutableSortedSet.copyOf(
            String.CASE_INSENSITIVE_ORDER, Splitter.on(',').trimResults().split(memoryFunctions));
    whitelistSet =
        ImmutableSortedSet.copyOf(
            String.CASE_INSENSITIVE_ORDER, Splitter.on(',').trimResults().split(whitelist));
  }

  @Override
  public DelphiCheckContext visit(PrimaryExpressionNode expression, DelphiCheckContext context) {
    if (shouldVisit(expression)) {
      expression.findChildrenOfType(NameReferenceNode.class).stream()
          .flatMap(reference -> reference.flatten().stream())
          .filter(MemoryManagementCheck::requiresMemoryManagement)
          .map(NameReferenceNode::getIdentifier)
          .filter(identifier -> !whitelistSet.contains(identifier.getImage()))
          .forEach(violationNode -> reportIssue(context, violationNode, MESSAGE));
    }

    return super.visit(expression, context);
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
    DelphiNode assignStatement = findParentSkipCasts(expression);
    if (assignStatement instanceof AssignmentStatementNode) {
      Type assignedType = ((AssignmentStatementNode) assignStatement).getAssignee().getType();
      return assignedType.isInterface();
    }
    return false;
  }

  private static boolean isInterfaceParameter(ExpressionNode expression) {
    DelphiNode parent = findParentSkipCasts(expression);

    if (!(parent instanceof ArgumentListNode)) {
      return false;
    }

    DelphiNode previous = parent.getParent().getChild(parent.getChildIndex() - 1);
    if (!(previous instanceof Typed)) {
      return false;
    }

    Type type = ((Typed) previous).getType();
    if (!type.isProcedural()) {
      return false;
    }

    DelphiNode argument = expression;
    while (argument.getParent() != parent) {
      argument = argument.getParent();
    }

    List<ExpressionNode> arguments = ((ArgumentListNode) parent).getArguments();
    int argumentIndex = Iterables.indexOf(arguments, argument::equals);

    Parameter parameter = ((ProceduralType) type).getParameter(argumentIndex);
    return parameter.getType().isInterface();
  }

  private static boolean isExceptionRaise(PrimaryExpressionNode expression) {
    return findParentSkipCasts(expression) instanceof RaiseStatementNode;
  }

  private boolean isMemoryManaged(PrimaryExpressionNode expression) {
    DelphiNode parent = findParentSkipCasts(expression);

    if (!(parent instanceof ArgumentListNode)) {
      return false;
    }

    DelphiNode node = parent.getParent().getChild(parent.getChildIndex() - 1);
    if (!(node instanceof NameReferenceNode)) {
      return false;
    }

    NameReferenceNode nameReference = ((NameReferenceNode) node).getLastName();
    NameDeclaration declaration = nameReference.getNameDeclaration();
    if (declaration instanceof MethodNameDeclaration) {
      var method = (MethodNameDeclaration) declaration;
      return memoryFunctionsSet.contains(method.fullyQualifiedName());
    }

    return false;
  }

  private static DelphiNode findParentSkipCasts(ExpressionNode expression) {
    DelphiNode result = getParentSkipParentheses(expression);
    while (true) {
      if (isSoftCast(result)) {
        result = getParentSkipParentheses(result);
      } else if (isHardCast(result)) {
        result = getNthParentSkipParentheses(result, 2);
      } else {
        return result;
      }
    }
  }

  private static DelphiNode getParentSkipParentheses(DelphiNode node) {
    return getNthParentSkipParentheses(node, 1);
  }

  private static DelphiNode getNthParentSkipParentheses(DelphiNode node, int n) {
    for (int i = 0; i < n; ++i) {
      if (node instanceof ExpressionNode) {
        node = ((ExpressionNode) node).findParentheses();
      }
      node = node.getParent();
    }
    return node;
  }

  private static boolean isSoftCast(DelphiNode node) {
    return node instanceof BinaryExpressionNode
        && ((BinaryExpressionNode) node).getOperator() == BinaryOperator.AS;
  }

  private static boolean isHardCast(DelphiNode node) {
    if (node instanceof ArgumentListNode) {
      ArgumentListNode argumentList = (ArgumentListNode) node;
      DelphiNode previous = argumentList.getParent().getChild(argumentList.getChildIndex() - 1);
      if (previous instanceof NameReferenceNode && isLastChild(argumentList)) {
        NameReferenceNode nameReference = ((NameReferenceNode) previous);
        NameDeclaration declaration = nameReference.getLastName().getNameDeclaration();
        return declaration instanceof TypeNameDeclaration;
      }
    }
    return false;
  }

  private static boolean isLastChild(DelphiNode node) {
    return node.getChildIndex() == node.getParent().getChildren().size() - 1;
  }

  private static boolean requiresMemoryManagement(NameReferenceNode reference) {
    NameDeclaration declaration = reference.getNameDeclaration();
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
