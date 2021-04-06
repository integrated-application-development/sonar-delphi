package org.sonar.plugins.delphi.pmd.rules;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.AsmStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ForLoopVarReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.delphi.antlr.ast.node.IdentifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.RaiseStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnaryExpressionNode;
import org.sonar.plugins.delphi.operator.UnaryOperator;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.type.Type;

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

  private static boolean isMethodStubWithStackUnwinding(MethodImplementationNode method) {
    MethodBodyNode body = method.getMethodBody();
    if (body.hasStatementBlock()) {
      for (StatementNode statement : body.getStatementBlock().getStatements()) {
        if (isStackUnwindingStatement(statement)) {
          return true;
        } else if (!(statement instanceof AssignmentStatementNode)) {
          return false;
        }
      }
    }
    return false;
  }

  private static boolean isStackUnwindingStatement(StatementNode statement) {
    return statement instanceof RaiseStatementNode || isAssertFalse(statement);
  }

  private static boolean isExitWithReturnValue(StatementNode statement) {
    return isMethodInvocation(statement, "System.Exit", arguments -> arguments.size() == 1);
  }

  private static boolean isAssertFalse(StatementNode statement) {
    return isMethodInvocation(statement, "System.Assert", arguments -> arguments.get(0).isFalse());
  }

  private static boolean isMethodInvocation(
      StatementNode statement,
      String fullyQualifiedName,
      Predicate<List<ExpressionNode>> argumentListPredicate) {
    if (!(statement instanceof ExpressionStatementNode)) {
      return false;
    }

    var expression = ((ExpressionStatementNode) statement).getExpression().skipParentheses();
    if (!(expression instanceof PrimaryExpressionNode) || expression.jjtGetNumChildren() > 2) {
      return false;
    }

    Node name = expression.jjtGetChild(0);
    if (!(name instanceof NameReferenceNode)) {
      return false;
    }

    NameDeclaration declaration = ((NameReferenceNode) name).getLastName().getNameDeclaration();
    List<ExpressionNode> arguments = extractArguments(expression.jjtGetChild(1));
    return declaration instanceof MethodNameDeclaration
        && ((MethodNameDeclaration) declaration).fullyQualifiedName().equals(fullyQualifiedName)
        && argumentListPredicate.test(arguments);
  }

  private static List<ExpressionNode> extractArguments(Node argumentList) {
    if (argumentList instanceof ArgumentListNode) {
      return ((ArgumentListNode) argumentList).getArguments();
    }
    return Collections.emptyList();
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
