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
package org.sonar.plugins.delphi.pmd.rules;

import net.sourceforge.pmd.RuleContext;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Checks if semicolons are properly placed
 */
public class NoSemicolonRule extends DelphiRule {

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (!isImplementationSection() || node.getType() != DelphiLexer.END) {
      return;
    }

    DelphiPMDNode violationNode = findViolationNode(node);
    if (violationNode != null) {
      addViolation(ctx, violationNode);
    }
  }

  private DelphiPMDNode findViolationNode(DelphiPMDNode node) {
    Tree previousNode = getPreviousNode(node);
    if (isValidNode(previousNode)) {
      if (isBlockNode(previousNode) && isMissingSemicolonInBlock(previousNode)) {
        return (DelphiPMDNode) previousNode.getChild(previousNode.getChildCount() - 1);
      } else if (notSemicolonNode(previousNode)) {
        return (DelphiPMDNode) previousNode;
      }
    }

    return null;
  }

  private boolean isValidNode(Tree node) {
    if (node == null) {
      return false;
    }

    return node.getType() != DelphiLexer.ELSE
        && node.getType() != DelphiLexer.FINALLY
        && node.getType() != DelphiLexer.IMPLEMENTATION
        && node.getType() != DelphiLexer.TkAssemblerInstructions;
  }

  private Tree getPreviousNode(DelphiPMDNode node) {
    int index = node.getChildIndex() - 1;
    if (index < 0 || node.getParent() == null) {
      return null;
    }

    return node.getParent().getChild(index);
  }

  private boolean isBlockNode(Tree node) {
    return node != null &&
        (node.getType() == DelphiLexer.BEGIN || node.getType() == DelphiLexer.EXCEPT);
  }

  private boolean isMissingSemicolonInBlock(Tree beginNode) {
    Tree lastChild = beginNode.getChild(beginNode.getChildCount() - 1);
    return notSemicolonNode(lastChild);
  }

  private boolean notSemicolonNode(Tree node) {
    return node == null || node.getType() != LexerMetrics.SEMI.toMetrics();
  }

}
