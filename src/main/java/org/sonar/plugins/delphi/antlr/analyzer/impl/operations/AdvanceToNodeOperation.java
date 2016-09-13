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
package org.sonar.plugins.delphi.antlr.analyzer.impl.operations;

import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.analyzer.CodeNode;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to advance to specific AST tree node
 */
public class AdvanceToNodeOperation implements NodeOperation {

  private List<LexerMetrics> to = new ArrayList<LexerMetrics>();

  /**
   * ctor
   * 
   * @param to Node type we want advance to
   */
  public AdvanceToNodeOperation(LexerMetrics to) {
    this.to.add(to);
  }

  /**
   * ctor
   * 
   * @param metricsList List of nodes we want to advance to
   */
  public AdvanceToNodeOperation(List<LexerMetrics> metricsList) {
    this.to = metricsList;
  }

  @Override
  public CodeNode<Tree> execute(Tree node) {
    CodeNode<Tree> atNode = new CodeNode<Tree>(node);
    do {
      atNode = new AdvanceNodeOperation().execute(atNode.getNode());
      if (atNode == null || !atNode.isValid()) {
        throw new IllegalStateException("Cannot advance to node type: " + createErrorMsg());
      }
    } while (shouldAdvanceFurther(atNode.getNode()));

    return atNode;
  }

  private String createErrorMsg() {
    StringBuilder str = new StringBuilder();
    for (LexerMetrics metric : to) {
      str.append(metric.toMetrics() + " ");
    }
    return str.toString();
  }

  private boolean shouldAdvanceFurther(Tree atNode) {
    int type = atNode.getType();
    for (LexerMetrics metric : to) {
      if (type == metric.toMetrics()) {
        return false;
      }
    }
    return true;
  }
}
