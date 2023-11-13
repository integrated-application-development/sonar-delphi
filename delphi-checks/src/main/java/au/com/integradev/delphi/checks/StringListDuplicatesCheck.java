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

import static java.util.function.Predicate.not;

import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.AssignmentStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.RoutineBodyNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "DuplicatesRule", repositoryKey = "delph")
@Rule(key = "StringListDuplicates")
public class StringListDuplicatesCheck extends DelphiCheck {
  private static final String MESSAGE =
      "Sort this 'TStringList' when setting the 'Duplicates' property.";

  @Override
  public DelphiCheckContext visit(RoutineBodyNode routineBody, DelphiCheckContext context) {
    if (routineBody.hasStatementBlock()) {
      routineBody
          .getStatementBlock()
          .descendantStatementStream()
          .filter(AssignmentStatementNode.class::isInstance)
          .map(AssignmentStatementNode.class::cast)
          .filter(StringListDuplicatesCheck::isDuplicatesStatement)
          .filter(not(StringListDuplicatesCheck::isSortedInSameBlock))
          .forEach(statement -> reportIssue(context, statement, MESSAGE));
    }
    return super.visit(routineBody, context);
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

  private static boolean isSortedInSameBlock(AssignmentStatementNode duplicates) {
    Node parent = duplicates.getParent();
    return parent instanceof StatementListNode
        && ((StatementListNode) parent)
            .statementStream()
            .anyMatch(statement -> isSortedStatement(duplicates, statement));
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

    if (!ExpressionNodeUtils.isTrue(sortedStatement.getValue())) {
      return false;
    }

    String listName = image.substring(0, image.length() - 7);
    String dupImage = duplicates.getAssignee().getImage();
    String dupListName = dupImage.substring(0, dupImage.length() - 11);

    return listName.equalsIgnoreCase(dupListName);
  }
}
