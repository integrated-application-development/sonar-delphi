package org.sonar.plugins.delphi.pmd.rules;

import java.util.Objects;
import net.sourceforge.pmd.RuleContext;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.StatementNode;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;

public class PascalStyleResultRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    MethodNameDeclaration methodNameDeclaration = method.getMethodNameDeclaration();
    if (methodNameDeclaration != null) {
      method.findDescendantsOfType(StatementNode.class).stream()
          .filter(AssignmentStatementNode.class::isInstance)
          .map(AssignmentStatementNode.class::cast)
          .map(AssignmentStatementNode::getAssignee)
          .map(ExpressionNode::extractSimpleNameReference)
          .filter(Objects::nonNull)
          .forEach(
              assignee -> {
                if (assignee.getNameDeclaration() == methodNameDeclaration) {
                  addViolation(data, assignee);
                }
              });
    }
    return super.visit(method, data);
  }
}
