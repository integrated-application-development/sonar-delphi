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
package au.com.integradev.delphi.checks;

import static au.com.integradev.delphi.utils.RoutineUtils.isStubRoutineWithStackUnwinding;
import static au.com.integradev.delphi.utils.StatementUtils.isRoutineInvocation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentNode;
import org.sonar.plugins.communitydelphi.api.ast.AsmStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.ast.IdentifierNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineBodyNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.ast.UnaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.operator.UnaryOperator;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "MethodResultAssignedRule", repositoryKey = "delph")
@DeprecatedRuleKey(ruleKey = "MethodResultAssigned", repositoryKey = "community-delphi")
@Rule(key = "RoutineResultAssigned")
public class RoutineResultAssignedCheck extends DelphiCheck {
  private static final String MESSAGE = "Assign this routine result.";

  private final Deque<RoutineResultContext> routineResultStack = new ArrayDeque<>();

  @Override
  public DelphiCheckContext visit(RoutineImplementationNode routine, DelphiCheckContext context) {
    routineResultStack.push(new RoutineResultContext(routine));

    super.visit(routine, context);

    RoutineResultContext resultContext = routineResultStack.pop();
    if (!isExcluded(routine)) {
      resultContext.addViolations(context);
    }

    return context;
  }

  @Override
  public DelphiCheckContext visit(AssignmentStatementNode assignment, DelphiCheckContext context) {
    NameReferenceNode assignee = extractSimpleNameReference(assignment.getAssignee());
    if (assignee != null && !handlePascalResultAssignments(assignee)) {
      handleResultReference(assignee);
    }
    return super.visit(assignment, context);
  }

  @Override
  public DelphiCheckContext visit(ForLoopVarReferenceNode loopVar, DelphiCheckContext context) {
    handleResultReference(loopVar.getNameReference());
    return super.visit(loopVar, context);
  }

  @Override
  public DelphiCheckContext visit(StatementNode statement, DelphiCheckContext context) {
    if (isExitWithReturnValue(statement)) {
      RoutineResultContext currentRoutine = routineResultStack.peek();
      if (currentRoutine != null) {
        markResultVariableAssigned(currentRoutine);
      }
    }
    return super.visit(statement, context);
  }

  @Override
  public DelphiCheckContext visit(ArgumentListNode argumentList, DelphiCheckContext context) {
    argumentList.getArgumentNodes().stream()
        .map(ArgumentNode::getExpression)
        .map(RoutineResultAssignedCheck::skipParenthesesAndAddressOperators)
        .forEach(this::handleResultReference);
    return super.visit(argumentList, context);
  }

  private static ExpressionNode skipParenthesesAndAddressOperators(ExpressionNode expression) {
    expression = expression.skipParentheses();
    if (expression instanceof UnaryExpressionNode) {
      UnaryExpressionNode unary = (UnaryExpressionNode) expression;
      if (unary.getOperator() == UnaryOperator.ADDRESS) {
        expression = skipParenthesesAndAddressOperators(unary.getOperand());
      }
    }
    return expression;
  }

  @Override
  public DelphiCheckContext visit(AsmStatementNode asmStatement, DelphiCheckContext context) {
    // Since we don't actually parse assembler statements, we have to do a bit of guesswork here.
    // Ideally we would eventually expand the grammar to include inline assembler statements.
    // Then we could resolve name references within them.
    for (IdentifierNode identifier : asmStatement.findDescendantsOfType(IdentifierNode.class)) {
      String image = identifier.getImage();
      markAssignedIf(result -> result.declaration.getImage().equalsIgnoreCase(image));
    }
    return super.visit(asmStatement, context);
  }

  private static boolean isExcluded(RoutineImplementationNode routine) {
    RoutineBodyNode body = routine.getRoutineBody();
    return body == null || isStubRoutineWithStackUnwinding(routine) || body.hasAsmBlock();
  }

  private static boolean isExitWithReturnValue(StatementNode statement) {
    return isRoutineInvocation(statement, "System.Exit", arguments -> arguments.size() == 1);
  }

  private boolean handlePascalResultAssignments(NameReferenceNode nameReference) {
    NameDeclaration declaration = nameReference.getNameDeclaration();
    Optional<RoutineResultContext> resultContext =
        routineResultStack.stream()
            .filter(context -> context.routineDeclaration == declaration)
            .findFirst();

    if (resultContext.isPresent()) {
      markResultVariableAssigned(resultContext.get());
      return true;
    }

    return false;
  }

  private void handleResultReference(ExpressionNode expression) {
    NameReferenceNode nameReference = extractSimpleNameReference(expression);
    if (nameReference != null) {
      handleResultReference(nameReference);
    }
  }

  private void handleResultReference(NameReferenceNode nameReference) {
    markAssignedIf(result -> result.declaration == nameReference.getNameDeclaration());
  }

  private void markResultVariableAssigned(RoutineResultContext context) {
    context.results.stream()
        .filter(result -> result.declaration.getImage().equals("Result"))
        .findFirst()
        .ifPresent(result -> result.assigned = true);
  }

  private void markAssignedIf(Predicate<RoutineResult> predicate) {
    for (RoutineResultContext context : routineResultStack) {
      for (RoutineResult result : context.results) {
        if (predicate.test(result)) {
          result.assigned = true;
          return;
        }
      }
    }
  }

  private static NameReferenceNode extractSimpleNameReference(ExpressionNode node) {
    node = node.skipParentheses();
    if (node instanceof PrimaryExpressionNode && node.getChildren().size() == 1) {
      DelphiNode child = node.getChild(0);
      if (child instanceof NameReferenceNode && ((NameReferenceNode) child).nextName() == null) {
        return (NameReferenceNode) child;
      }
    }
    return null;
  }

  private final class RoutineResultContext {
    private final RoutineNameDeclaration routineDeclaration;
    private final List<RoutineResult> results;

    private RoutineResultContext(RoutineImplementationNode routine) {
      routineDeclaration = routine.getRoutineNameDeclaration();
      results =
          routine.getParameters().stream()
              .filter(FormalParameterData::isOut)
              .map(FormalParameterData::getNode)
              .filter(n -> n.getNameDeclaration() instanceof VariableNameDeclaration)
              .map(n -> new RoutineResult((VariableNameDeclaration) n.getNameDeclaration(), n))
              .collect(Collectors.toList());

      if (routine.isFunction()) {
        routine.getScope().getVariableDeclarations().stream()
            .filter(declaration -> declaration.getImage().equals("Result"))
            .findFirst()
            .ifPresent(
                declaration -> {
                  RoutineResult result =
                      new RoutineResult(declaration, routine.getRoutineNameNode());
                  results.add(result);
                });
      }
    }

    private void addViolations(DelphiCheckContext context) {
      results.stream()
          .filter(result -> !result.assigned)
          .forEach(result -> reportIssue(context, result.location, MESSAGE));
    }
  }

  private static final class RoutineResult {
    private final VariableNameDeclaration declaration;
    private final DelphiNode location;
    private boolean assigned;

    private RoutineResult(VariableNameDeclaration declaration, DelphiNode location) {
      Type type = declaration.getType();
      this.declaration = declaration;
      this.location = location;
      this.assigned = type.isRecord() || type.isFixedArray();
    }
  }
}
