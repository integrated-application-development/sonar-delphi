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

import au.com.integradev.delphi.antlr.ast.node.AnonymousMethodNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.RoutineImplementationNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.StatementListNodeImpl;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FinalizationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.InitializationSectionNode;

public final class ControlFlowGraphUtils {
  private ControlFlowGraphUtils() {
    // Utility class
  }

  private static ControlFlowGraphProvider getCFGProvider(DelphiNode node) {
    if (node instanceof RoutineImplementationNodeImpl) {
      return (ControlFlowGraphProvider) node;
    }
    if (node instanceof AnonymousMethodNodeImpl) {
      return (ControlFlowGraphProvider) node;
    }
    if (node instanceof CompoundStatementNode
        && node.getParent() instanceof DelphiAst
        && ((CompoundStatementNode) node).getStatementList() instanceof StatementListNodeImpl) {
      return (ControlFlowGraphProvider) ((CompoundStatementNode) node).getStatementList();
    }
    if (node instanceof StatementListNodeImpl
        && (node.getParent() instanceof InitializationSectionNode
            || node.getParent() instanceof FinalizationSectionNode)) {
      return (ControlFlowGraphProvider) node;
    }
    return null;
  }

  public static ControlFlowGraph findContainingCFG(DelphiNode node) {
    while (node != null) {
      ControlFlowGraphProvider cfgSupplier = getCFGProvider(node);
      if (cfgSupplier != null) {
        return cfgSupplier.getControlFlowGraph();
      }
      node = node.getParent();
    }
    return null;
  }
}
