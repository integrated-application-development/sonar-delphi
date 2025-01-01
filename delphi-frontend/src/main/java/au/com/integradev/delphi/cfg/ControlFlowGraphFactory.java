/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
package au.com.integradev.delphi.cfg;

import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import com.google.common.collect.Lists;
import java.util.List;
import org.sonar.plugins.communitydelphi.api.ast.AnonymousMethodNode;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.RoutineImplementationNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementNode;

public final class ControlFlowGraphFactory {
  private ControlFlowGraphFactory() {
    // Utility class
  }

  public static ControlFlowGraph create(RoutineImplementationNode routine) {
    return create(routine.getRoutineBody().getStatementBlock());
  }

  public static ControlFlowGraph create(AnonymousMethodNode anonymousMethod) {
    return create(anonymousMethod.getFirstDescendantOfType(CompoundStatementNode.class));
  }

  public static ControlFlowGraph create(CompoundStatementNode initialNode) {
    return create(initialNode.getStatementList());
  }

  public static ControlFlowGraph create(StatementListNode statements) {
    return create(statements.getStatements());
  }

  public static ControlFlowGraph create(List<StatementNode> statements) {
    ControlFlowGraphBuilder builder = new ControlFlowGraphBuilder();
    ControlFlowGraphVisitor visitor = new ControlFlowGraphVisitor();
    Lists.reverse(statements).forEach(statement -> statement.accept(visitor, builder));
    return builder.build();
  }
}
