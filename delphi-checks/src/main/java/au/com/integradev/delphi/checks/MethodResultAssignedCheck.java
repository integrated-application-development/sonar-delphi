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

import static au.com.integradev.delphi.utils.MethodUtils.isMethodStubWithStackUnwinding;
import static au.com.integradev.delphi.utils.StatementUtils.isMethodInvocation;

import au.com.integradev.delphi.operator.UnaryOperator;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.AsmStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.ForLoopVarReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.ast.IdentifierNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodBodyNode;
import org.sonar.plugins.communitydelphi.api.ast.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.ast.UnaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "MethodResultAssignedRule", repositoryKey = "delph")
@Rule(key = "MethodResultAssigned")
public class MethodResultAssignedCheck extends DelphiCheck {
  private static final String MESSAGE = "Assign this method result.";

  private final Deque<MethodResultContext> methodResultStack = new ArrayDeque<>();

  @Override
  public DelphiCheckContext visit(MethodImplementationNode method, DelphiCheckContext context) {
    methodResultStack.push(new MethodResultContext(method));

    super.visit(method, context);

    MethodResultContext resultContext = methodResultStack.pop();
    if (!isExcluded(method)) {
      resultContext.addViolations(context);
    }

    return context;
  }

  @Override
  public DelphiCheckContext visit(AssignmentStatementNode assignment, DelphiCheckContext context) {
    NameReferenceNode assignee = assignment.getAssignee().extractSimpleNameReference();
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
      MethodResultContext currentMethod = methodResultStack.peek();
      if (currentMethod != null) {
        markResultVariableAssigned(currentMethod);
      }
    }
    return super.visit(statement, context);
  }

  @Override
  public DelphiCheckContext visit(ArgumentListNode argumentList, DelphiCheckContext context) {
    argumentList.getArguments().stream()
        .map(MethodResultAssignedCheck::skipParenthesesAndAddressOperators)
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

  private static boolean isExcluded(MethodImplementationNode method) {
    MethodBodyNode body = method.getMethodBody();
    return body == null || isMethodStubWithStackUnwinding(method) || body.hasAsmBlock();
  }

  private static boolean isExitWithReturnValue(StatementNode statement) {
    return isMethodInvocation(statement, "System.Exit", arguments -> arguments.size() == 1);
  }

  private boolean handlePascalResultAssignments(NameReferenceNode nameReference) {
    NameDeclaration declaration = nameReference.getNameDeclaration();
    Optional<MethodResultContext> resultContext =
        methodResultStack.stream()
            .filter(context -> context.methodDeclaration == declaration)
            .findFirst();

    if (resultContext.isPresent()) {
      markResultVariableAssigned(resultContext.get());
      return true;
    }

    return false;
  }

  private void handleResultReference(ExpressionNode expression) {
    NameReferenceNode nameReference = expression.extractSimpleNameReference();
    if (nameReference != null) {
      handleResultReference(nameReference);
    }
  }

  private void handleResultReference(NameReferenceNode nameReference) {
    markAssignedIf(result -> result.declaration == nameReference.getNameDeclaration());
  }

  private void markResultVariableAssigned(MethodResultContext context) {
    context.results.stream()
        .filter(result -> result.declaration.getImage().equals("Result"))
        .findFirst()
        .ifPresent(result -> result.assigned = true);
  }

  private void markAssignedIf(Predicate<MethodResult> predicate) {
    for (MethodResultContext context : methodResultStack) {
      for (MethodResult result : context.results) {
        if (predicate.test(result)) {
          result.assigned = true;
          return;
        }
      }
    }
  }

  private final class MethodResultContext {
    private final MethodNameDeclaration methodDeclaration;
    private final List<MethodResult> results;

    private MethodResultContext(MethodImplementationNode method) {
      methodDeclaration = method.getMethodNameDeclaration();
      results =
          method.getParameters().stream()
              .filter(FormalParameterData::isOut)
              .map(FormalParameterData::getNode)
              .filter(n -> n.getNameDeclaration() instanceof VariableNameDeclaration)
              .map(n -> new MethodResult((VariableNameDeclaration) n.getNameDeclaration(), n))
              .collect(Collectors.toList());

      if (method.isFunction()) {
        method.getScope().getVariableDeclarations().stream()
            .filter(declaration -> declaration.getImage().equals("Result"))
            .findFirst()
            .ifPresent(
                declaration -> {
                  MethodResult result = new MethodResult(declaration, method.getMethodNameNode());
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

  private static final class MethodResult {
    private final VariableNameDeclaration declaration;
    private final DelphiNode location;
    private boolean assigned;

    private MethodResult(VariableNameDeclaration declaration, DelphiNode location) {
      Type type = declaration.getType();
      this.declaration = declaration;
      this.location = location;
      this.assigned = type.isRecord() || type.isFixedArray();
    }
  }
}
