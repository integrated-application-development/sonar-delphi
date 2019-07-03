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
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;

/**
 * Class for counting method lines. If too long, creates a violation.
 */
public class TooLongMethodRule extends DelphiRule {

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (notMethodNode(node)){
      return;
    }

    Tree beginNode = findBeginNode(node);

    if (beginNode != null) {
      int firstLine = node.getLine();
      int lastLine = getLastLine(beginNode);
      int lines = lastLine - firstLine;
      int limit = getProperty(LIMIT);

      if (lines > limit) {
        String methodName = getMethodName(node);
        String msg =
            methodName + " is too long (" + lines + " lines). Maximum line count is " + limit;
        addViolation(ctx, node, msg);
        lastLineParsed = lastLine;
      }
    }
  }

  private boolean notMethodNode(DelphiPMDNode node) {
    return (node.getType() != DelphiLexer.PROCEDURE && node.getType() != DelphiLexer.FUNCTION);
  }

  private Tree findBeginNode(DelphiPMDNode parent) {
    for (int i = parent.getChildIndex() + 1; i < parent.getParent().getChildCount(); ++i) {
      Tree sibling = parent.getParent().getChild(i);
      int type = sibling.getType();

      if (type == DelphiLexer.BEGIN) {
        return sibling;
      }

      if (type != DelphiLexer.VAR && type != DelphiLexer.CONST) {
        break;
      }
    }

    return null;
  }

  private int getLastLine(Tree node) {
    int line = -1;
    for (int i = 0; i < node.getChildCount(); ++i) {
      Tree child = node.getChild(i);
      if (child.getLine() > line) {
        line = child.getLine();
      }
      if (child.getType() == DelphiLexer.BEGIN) {
        line = getLastLine(child);
      } else if (child.getType() == DelphiLexer.END) {
        return line;
      }
    }

    return line;
  }

  private String getMethodName(DelphiPMDNode node) {
    // get method name
    StringBuilder methodName = new StringBuilder();
    Tree nameNode = node.getFirstChildWithType(DelphiLexer.TkFunctionName);
    if (nameNode != null) {
      for (int c = 0; c < nameNode.getChildCount(); ++c) {
        methodName.append(nameNode.getChild(c).getText());
      }
    } else {
      throw new IllegalStateException("No method name found for TooLongMethodRule.");
    }

    return methodName.toString();
  }
}
