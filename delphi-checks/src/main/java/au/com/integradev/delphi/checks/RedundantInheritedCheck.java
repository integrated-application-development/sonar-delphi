/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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

import au.com.integradev.delphi.utils.RoutineUtils;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFix;
import org.sonar.plugins.communitydelphi.api.reporting.QuickFixEdit;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;

@Rule(key = "RedundantInherited")
public class RedundantInheritedCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this redundant inherited call.";

  @Override
  public DelphiCheckContext visit(RoutineImplementationNode routine, DelphiCheckContext context) {
    for (DelphiNode violationNode : findViolations(routine)) {
      context
          .newIssue()
          .onNode(violationNode)
          .withMessage(MESSAGE)
          .withQuickFixes(getQuickFixes(violationNode, context))
          .report();
    }
    return super.visit(routine, context);
  }

  private static List<QuickFix> getQuickFixes(
      DelphiNode violationNode, DelphiCheckContext context) {
    DelphiNode violationStatement = violationNode;
    DelphiNode parent = violationNode.getParent();
    while (!(parent instanceof StatementListNode)) {
      violationStatement = parent;
      parent = parent.getParent();
      if (parent == null) {
        return Collections.emptyList();
      }
    }
    StatementListNode statementListNode = (StatementListNode) parent;
    int statementIndex = statementListNode.getStatements().indexOf(violationStatement);

    int startLine = violationStatement.getBeginLine();
    int startCol = violationStatement.getBeginColumn();

    DelphiToken lastStatementToken = getLastStatementToken(statementListNode, violationStatement);
    int endLine = lastStatementToken.getEndLine();
    int endCol = lastStatementToken.getEndColumn();

    if (statementListNode.getStatements().size() > statementIndex + 1) {
      DelphiToken lastDeletableToken = nextNonWhitespaceToken(context, lastStatementToken, true);
      endLine = lastDeletableToken.getBeginLine();
      endCol = lastDeletableToken.getBeginColumn();
    } else if (statementIndex > 0) {
      DelphiToken lastDeletableToken =
          nextNonWhitespaceToken(context, violationStatement.getFirstToken(), false);
      startLine = lastDeletableToken.getEndLine();
      startCol = lastDeletableToken.getEndColumn();
    }

    return List.of(
        QuickFix.newFix("Remove redundant inherited call")
            .withEdit(
                QuickFixEdit.delete(FilePosition.from(startLine, startCol, endLine, endCol))));
  }

  private static DelphiToken nextNonWhitespaceToken(
      DelphiCheckContext context, DelphiToken startingToken, boolean forward) {
    int step = forward ? 1 : -1;
    int contextIndex = getContextIndex(context, startingToken);
    if (contextIndex == -1) {
      return startingToken;
    }

    do {
      contextIndex += step;
    } while (context.getTokens().get(contextIndex).getType() == DelphiTokenType.WHITESPACE);
    return context.getTokens().get(contextIndex);
  }

  private static boolean isSemicolon(DelphiNode node) {
    return node.getChildren().isEmpty() && node.getToken().getType() == DelphiTokenType.SEMICOLON;
  }

  private static int getContextIndex(DelphiCheckContext context, DelphiToken token) {
    return IntStream.range(0, context.getTokens().size())
        .filter(tokenIndex -> context.getTokens().get(tokenIndex).getIndex() == token.getIndex())
        .findFirst()
        .orElse(-1);
  }

  private static DelphiToken getLastStatementToken(
      StatementListNode statementListNode, DelphiNode node) {
    return statementListNode.getChildren().stream()
        .skip(node.getChildIndex() + 1L)
        .takeWhile(RedundantInheritedCheck::isSemicolon)
        .reduce((first, second) -> second)
        .orElse(node)
        .getLastToken();
  }

  private static List<DelphiNode> findViolations(RoutineImplementationNode routine) {
    RoutineNameDeclaration nameDeclaration = routine.getRoutineNameDeclaration();
    if (nameDeclaration != null && nameDeclaration.hasDirective(RoutineDirective.MESSAGE)) {
      // HACK:
      //   Calls to inherited don't currently resolve to parent message handlers with different
      //   signatures.
      //   See: https://github.com/integrated-application-development/sonar-delphi/issues/271
      return Collections.emptyList();
    }

    CompoundStatementNode block = routine.getStatementBlock();
    if (block == null) {
      return Collections.emptyList();
    }

    List<StatementNode> statements = block.getDescendentStatements();
    if (statements.isEmpty()) {
      return Collections.emptyList();
    }

    List<RoutineNameDeclaration> inheritedMethods =
        RoutineUtils.findParentMethodDeclarations(routine);
    if (!inheritedMethods.isEmpty()) {
      return Collections.emptyList();
    }

    return statements.stream()
        .filter(ExpressionStatementNode.class::isInstance)
        .map(statement -> ((ExpressionStatementNode) statement).getExpression())
        .filter(ExpressionNodeUtils::isBareInherited)
        .collect(Collectors.toList());
  }
}
