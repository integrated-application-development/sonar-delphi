package org.sonar.plugins.delphi.pmd.rules;

import static java.util.function.Predicate.not;

import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.ast.Node;
import org.sonar.plugins.delphi.antlr.ast.node.AssignmentStatementNode;
import org.sonar.plugins.delphi.antlr.ast.node.MethodBodyNode;

/**
 * This rule adds violations when the Duplicates method, (foo.Duplicates := dupError) is called on a
 * list, but the preceding line did not first sort the list (using foo.Sorted := True)
 */
public class DuplicatesRule extends AbstractDelphiRule {

  @Override
  public RuleContext visit(MethodBodyNode methodBody, RuleContext data) {
    if (methodBody.hasStatementBlock()) {
      methodBody
          .getStatementBlock()
          .descendantStatementStream()
          .filter(AssignmentStatementNode.class::isInstance)
          .map(AssignmentStatementNode.class::cast)
          .filter(DuplicatesRule::isDuplicatesStatement)
          .filter(not(DuplicatesRule::isSortedByPreviousStatement))
          .filter(not(DuplicatesRule::isSortedByNextStatement))
          .forEach(statement -> addViolation(data, statement));
    }
    return super.visit(methodBody, data);
  }

  private static boolean isDuplicatesStatement(AssignmentStatementNode duplicates) {
    String valueImage = duplicates.getValue().getImage();
    if (valueImage.equalsIgnoreCase("dupAccept")) {
      return false;
    }

    String assigneeImage = duplicates.getAssignee().getImage();
    if (assigneeImage.length() < 11) {
      return false;
    }

    return assigneeImage.regionMatches(true, assigneeImage.length() - 11, ".Duplicates", 0, 11);
  }

  private static boolean isSortedByPreviousStatement(AssignmentStatementNode duplicates) {
    Node previous = duplicates.jjtGetParent().jjtGetChild(duplicates.jjtGetChildIndex() - 2);
    return isSortedStatement(duplicates, previous);
  }

  private static boolean isSortedByNextStatement(AssignmentStatementNode duplicates) {
    Node previous = duplicates.jjtGetParent().jjtGetChild(duplicates.jjtGetChildIndex() + 2);
    return isSortedStatement(duplicates, previous);
  }

  private static boolean isSortedStatement(AssignmentStatementNode duplicates, Node statement) {
    if (!(statement instanceof AssignmentStatementNode)) {
      return false;
    }

    AssignmentStatementNode sortedStatement = (AssignmentStatementNode) statement;

    String image = sortedStatement.getAssignee().getImage();
    if (image.length() < 7) {
      return false;
    }

    if (!sortedStatement.getValue().isTrue()) {
      return false;
    }

    String listName = image.substring(0, image.length() - 7);
    String dupImage = duplicates.getAssignee().getImage();
    String dupListName = dupImage.substring(0, dupImage.length() - 11);

    return listName.equalsIgnoreCase(dupListName);
  }
}
