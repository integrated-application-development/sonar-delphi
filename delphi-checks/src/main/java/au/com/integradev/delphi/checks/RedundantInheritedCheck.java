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
import org.sonar.check.Rule;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.ExpressionStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;
import org.sonar.plugins.communitydelphi.api.ast.utils.ExpressionNodeUtils;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheck;
import org.sonar.plugins.communitydelphi.api.check.DelphiCheckContext;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;

@Rule(key = "RedundantInherited")
public class RedundantInheritedCheck extends DelphiCheck {
  private static final String MESSAGE = "Remove this redundant inherited call.";

  @Override
  public DelphiCheckContext visit(RoutineImplementationNode routine, DelphiCheckContext context) {
    for (DelphiNode violationNode : findViolations(routine)) {
      reportIssue(context, violationNode, MESSAGE);
    }
    return super.visit(routine, context);
  }

  private static List<DelphiNode> findViolations(RoutineImplementationNode routine) {
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
        .filter((statement) -> statement instanceof ExpressionStatementNode)
        .map((statement) -> ((ExpressionStatementNode) statement).getExpression())
        .filter(ExpressionNodeUtils::isBareInherited)
        .collect(Collectors.toList());
  }
}
