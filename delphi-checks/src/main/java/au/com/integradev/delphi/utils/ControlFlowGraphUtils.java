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
package au.com.integradev.delphi.utils;

import au.com.integradev.delphi.antlr.ast.node.AnonymousMethodNodeImpl;
import au.com.integradev.delphi.antlr.ast.node.RoutineImplementationNodeImpl;
import au.com.integradev.delphi.cfg.ControlFlowGraphFactory;
import au.com.integradev.delphi.cfg.api.ControlFlowGraph;
import java.util.function.Supplier;
import org.sonar.plugins.communitydelphi.api.ast.CompoundStatementNode;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.FinalizationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.InitializationSectionNode;
import org.sonar.plugins.communitydelphi.api.ast.StatementListNode;

public final class ControlFlowGraphUtils {
  private ControlFlowGraphUtils() {
    // Utility class
  }

  private static Supplier<ControlFlowGraph> getCFGSupplier(DelphiNode node) {
    if (node instanceof RoutineImplementationNodeImpl) {
      return ((RoutineImplementationNodeImpl) node)::getControlFlowGraph;
    }
    if (node instanceof AnonymousMethodNodeImpl) {
      return ((AnonymousMethodNodeImpl) node)::getControlFlowGraph;
    }
    if (node instanceof CompoundStatementNode && node.getParent() instanceof DelphiAst) {
      return () -> ControlFlowGraphFactory.create((CompoundStatementNode) node);
    }
    if (node instanceof StatementListNode
        && (node.getParent() instanceof InitializationSectionNode
            || node.getParent() instanceof FinalizationSectionNode)) {
      return () -> ControlFlowGraphFactory.create((StatementListNode) node);
    }
    return null;
  }

  public static ControlFlowGraph findContainingCFG(DelphiNode node) {
    while (node != null) {
      Supplier<ControlFlowGraph> cfgSupplier = getCFGSupplier(node);
      if (cfgSupplier != null) {
        return cfgSupplier.get();
      }
      node = node.getParent();
    }
    return null;
  }
}
