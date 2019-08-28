package org.sonar.plugins.delphi.pmd.rules;

import java.util.Collections;
import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.ast.node.ArgumentListNode;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.CompoundStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameter;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;

public class InheritedMethodWithNoCodeRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    DelphiNode violationNode = findViolation(method);
    if (violationNode != null) {
      addViolation(data, violationNode);
    }

    return super.visit(method, data);
  }

  private static DelphiNode findViolation(MethodImplementationNode method) {
    CompoundStatementNode block = method.getMethodBody().getStatementBlock();
    if (block == null) {
      return null;
    }

    List<StatementNode> statements = block.getStatements();
    if (statements.size() != 1) {
      return null;
    }

    StatementNode statement = statements.get(0);
    ExpressionNode expr = null;

    if (statement instanceof ExpressionStatementNode) {
      expr = ((ExpressionStatementNode) statement).getExpression();
    } else if (statement instanceof AssignmentStatementNode) {
      AssignmentStatementNode assignment = (AssignmentStatementNode) statement;
      if (assignment.getAssignee().isResult()) {
        expr = assignment.getValue();
      }
    }

    if (isInheritedCall(method, expr)) {
      return statement;
    }

    return null;
  }

  private static boolean isInheritedCall(MethodImplementationNode method, ExpressionNode expr) {
    if (!(expr instanceof PrimaryExpressionNode)) {
      return false;
    }

    PrimaryExpressionNode expression = (PrimaryExpressionNode) expr;
    if (expression.isBareInherited()) {
      return true;
    }

    if (!expression.isInheritedCall()) {
      return false;
    }

    Node reference = expression.jjtGetChild(1);
    String methodName = method.simpleName();
    if (!(reference instanceof NameReferenceNode && reference.hasImageEqualTo(methodName))) {
      return false;
    }

    Node argumentList = expression.jjtGetChild(2);
    if (argumentList != null && !(argumentList instanceof ArgumentListNode)) {
      return false;
    }

    return argumentSignaturesMatch(method, (ArgumentListNode) argumentList);
  }

  private static boolean argumentSignaturesMatch(MethodNode method, ArgumentListNode argumentList) {
    List<FormalParameter> parameters = method.getParameters();
    List<ExpressionNode> arguments =
        (argumentList == null) ? Collections.emptyList() : argumentList.getArguments();

    if (arguments.size() != parameters.size()) {
      return false;
    }

    for (int i = 0; i < arguments.size(); ++i) {
      if (!arguments.get(i).hasImageEqualTo(parameters.get(i).getImage())) {
        return false;
      }
    }

    return true;
  }
}
