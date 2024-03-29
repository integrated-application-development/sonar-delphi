/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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

import com.google.common.collect.Iterables;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.CaseItemStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExceptItemNode;
import org.sonar.plugins.communitydelphi.api.ast.FieldDeclarationNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.RecordVariantItemNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineHeadingNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonarsource.analyzer.commons.annotations.DeprecatedRuleKey;

@DeprecatedRuleKey(ruleKey = "NoSemicolonRule", repositoryKey = "delph")
@DeprecatedRuleKey(ruleKey = "NoSemiAfterFieldDeclarationRuleTest", repositoryKey = "delph")
@DeprecatedRuleKey(ruleKey = "NoSemiAfterMethodDeclarationRuleTest", repositoryKey = "delph")
@Rule(key = "MissingSemicolon")
public class MissingSemicolonCheck extends DelphiCheck {
  @Override
  public DelphiCheckContext visit(FieldDeclarationNode field, DelphiCheckContext context) {
    if (!(field.getParent() instanceof RecordVariantItemNode)) {
      DelphiNode lastChild = Iterables.getLast(field.getChildren());
      if (lastChild.getTokenType() != DelphiTokenType.SEMICOLON) {
        reportIssue(context, field, "Terminate this field declaration with a semicolon.");
      }
    }
    return super.visit(field, context);
  }

  @Override
  public DelphiCheckContext visit(RoutineHeadingNode heading, DelphiCheckContext context) {
    DelphiNode lastChild = Iterables.getLast(heading.getChildren());
    if (lastChild.getTokenType() != DelphiTokenType.SEMICOLON) {
      reportIssue(context, heading, "Terminate this routine heading with a semicolon.");
    }
    return super.visit(heading, context);
  }

  @Override
  public DelphiCheckContext visit(StatementNode node, DelphiCheckContext context) {
    if (shouldVisitStatement(node)) {
      DelphiNode violationNode = findStatementIssueNode(node);

      if (violationNode != null) {
        reportIssue(context, violationNode, "Terminate this statement with a semicolon.");
      }
    }

    return super.visit(node, context);
  }

  private static boolean shouldVisitStatement(DelphiNode node) {
    DelphiNode parent = node.getParent();
    return parent instanceof CaseItemStatementNode
        || parent instanceof ExceptItemNode
        || parent instanceof StatementListNode;
  }

  private static DelphiNode findStatementIssueNode(DelphiNode node) {
    Node nextNode = node.getParent().getChild(node.getChildIndex() + 1);
    if (nextNode == null || nextNode.getTokenType() != DelphiTokenType.SEMICOLON) {
      return findNodePrecedingMissingSemicolon(node);
    }
    return null;
  }

  private static DelphiNode findNodePrecedingMissingSemicolon(DelphiNode node) {
    DelphiNode lastNode = node;
    int childCount = lastNode.getChildren().size();

    while (childCount > 0) {
      lastNode = Iterables.getLast(lastNode.getChildren());
      childCount = lastNode.getChildren().size();
    }

    return lastNode;
  }
}
