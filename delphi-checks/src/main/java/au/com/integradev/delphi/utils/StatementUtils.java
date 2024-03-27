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
package au.com.integradev.delphi.utils;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentListNode;
import org.sonar.plugins.communitydelphi.api.ast.ArgumentNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;

public final class StatementUtils {
  private StatementUtils() {
    // Utility class
  }

  public static boolean isRoutineInvocation(
      StatementNode statement,
      String fullyQualifiedName,
      Predicate<List<ArgumentNode>> argumentListPredicate) {
    if (!(statement instanceof ExpressionStatementNode)) {
      return false;
    }

    var expression = ((ExpressionStatementNode) statement).getExpression().skipParentheses();
    if (!(expression instanceof PrimaryExpressionNode) || expression.getChildren().size() > 2) {
      return false;
    }

    Node name = expression.getChild(0);
    if (!(name instanceof NameReferenceNode)) {
      return false;
    }

    NameDeclaration declaration = ((NameReferenceNode) name).getLastName().getNameDeclaration();
    List<ArgumentNode> arguments = extractArguments(expression.getChild(1));
    return declaration instanceof RoutineNameDeclaration
        && ((RoutineNameDeclaration) declaration).fullyQualifiedName().equals(fullyQualifiedName)
        && argumentListPredicate.test(arguments);
  }

  private static List<ArgumentNode> extractArguments(Node argumentList) {
    if (argumentList instanceof ArgumentListNode) {
      return ((ArgumentListNode) argumentList).getArgumentNodes();
    }
    return Collections.emptyList();
  }
}
