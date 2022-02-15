package org.sonar.plugins.delphi.pmd.rules;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.ExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodImplementationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationListNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.VarStatementNode;

public class UnusedLocalVariablesRule extends AbstractDelphiRule {
  @Override
  public RuleContext visit(MethodImplementationNode method, RuleContext data) {
    Set<NameOccurrence> excludedOccurrences = new HashSet<>();
    for (var assignment : method.findDescendantsOfType(AssignmentStatementNode.class)) {
      ExpressionNode assigneee = assignment.getAssignee();
      if (assigneee instanceof PrimaryExpressionNode) {
        assigneee.findChildrenOfType(NameReferenceNode.class).stream()
            .map(NameReferenceNode::getNameOccurrence)
            .filter(Objects::nonNull)
            .forEach(excludedOccurrences::add);
      }
    }

    List<NameDeclarationNode> declarations = new ArrayList<>();
    method.findDescendantsOfType(VarDeclarationNode.class).stream()
        .map(VarDeclarationNode::getNameDeclarationList)
        .map(NameDeclarationListNode::getDeclarations)
        .forEach(declarations::addAll);
    method.findDescendantsOfType(VarStatementNode.class).stream()
        .map(VarStatementNode::getNameDeclarationList)
        .map(NameDeclarationListNode::getDeclarations)
        .forEach(declarations::addAll);

    declarations.stream()
        .filter(node -> excludedOccurrences.containsAll(node.getUsages()))
        .forEach(node -> addViolation(data, node));

    return data;
  }
}
