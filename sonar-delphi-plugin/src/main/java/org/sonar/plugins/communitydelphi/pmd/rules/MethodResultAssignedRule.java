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
package org.sonar.plugins.communitydelphi.pmd.rules;

import static org.sonar.plugins.communitydelphi.utils.MethodUtils.isMethodStubWithStackUnwinding;
import static org.sonar.plugins.communitydelphi.utils.StatementUtils.isMethodInvocation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.communitydelphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.AsmStatementNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.ForLoopVarReferenceNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.antlr.ast.node.IdentifierNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.StatementNode;
import org.sonar.plugins.communitydelphi.antlr.ast.node.UnaryExpressionNode;
import org.sonar.plugins.communitydelphi.operator.UnaryOperator;
import org.sonar.plugins.communitydelphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.type.Type;

public class MethodResultAssignedRule extends AbstractDelphiRule {
  private final Deque<MethodResultContext> methodResultStack = new ArrayDeque<>();

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    methodResultStack.push(new MethodResultContext(method));

    super.visit(method, data);

    MethodResultContext context = methodResultStack.pop();
    if (!isExcluded(method)) {
      context.addViolations(data);
    }

    return data;
  }

  @Override
  public RuleContext visit(AssignmentStatementNode assignment, RuleContext data) {
    NameReferenceNode assignee = assignment.getAssignee().extractSimpleNameReference();
    if (assignee != null && !handlePascalResultAssignments(assignee)) {
      handleResultReference(assignee);
    }
    return super.visit(assignment, data);
  }

  @Override
  public RuleContext visit(ForLoopVarReferenceNode loopVar, RuleContext data) {
    handleResultReference(loopVar.getNameReference());
    return super.visit(loopVar, data);
  }

  @Override
  public RuleContext visit(StatementNode statement, RuleContext data) {
    if (isExitWithReturnValue(statement)) {
      MethodResultContext currentMethod = methodResultStack.peek();
      if (currentMethod != null) {
        markResultVariableAssigned(currentMethod);
      }
    }
    return super.visit(statement, data);
  }

  @Override
  public RuleContext visit(ArgumentListNode argumentList, RuleContext data) {
    argumentList.getArguments().stream()
        .map(MethodResultAssignedRule::skipParenthesesAndAddressOperators)
        .forEach(this::handleResultReference);
    return super.visit(argumentList, data);
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
  public RuleContext visit(AsmStatementNode asmStatement, RuleContext data) {
    // Since we don't actually parse assembler statements, we have to do a bit of guesswork here.
    // Ideally we would eventually expand the grammar to include inline assembler statements.
    // Then we could resolve name references within them.
    for (IdentifierNode identifier : asmStatement.findDescendantsOfType(IdentifierNode.class)) {
      String image = identifier.getImage();
      markAssignedIf(result -> result.declaration.getImage().equalsIgnoreCase(image));
    }
    return super.visit(asmStatement, data);
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

  private class MethodResultContext {
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

    private void addViolations(RuleContext data) {
      results.stream()
          .filter(result -> !result.assigned)
          .forEach(result -> addViolation(data, result.location));
    }
  }

  private static class MethodResult {
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
