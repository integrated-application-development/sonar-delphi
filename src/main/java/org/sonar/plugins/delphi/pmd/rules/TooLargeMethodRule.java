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
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

/** Class for counting method statements. If too many, creates a violation. */
public class TooLargeMethodRule extends DelphiRule {
  private static final String VIOLATION_MESSAGE =
      "%s is too large. Method has %d statements (Limit is %d)";

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    if (shouldSkip(node)) {
      return;
    }

    Tree beginNode = findBeginNode(node);

    if (beginNode != null) {
      int statements = countStatements(beginNode);
      int limit = getProperty(LIMIT);

      if (statements > limit) {
        String methodName = getMethodName(node);
        addViolation(ctx, node, String.format(VIOLATION_MESSAGE, methodName, statements, limit));
      }
    }
  }

  private int countStatements(Tree node) {
    if (node.getType() == DelphiLexer.BEGIN) {
      return countCompoundStatement(node);
    }

    int count = 0;

    if (isStatementType(node)) {
      ++count;
    }

    if (isStatementTerminator(node)) {
      ++count;
    }

    return count;
  }

  private int countCompoundStatement(Tree node) {
    int count = 0;

    for (int i = 0; i < node.getChildCount(); ++i) {
      count += countStatements(node.getChild(i));
    }

    return count;
  }

  private boolean isStatementType(Tree node) {
    switch (node.getType()) {
      case DelphiLexer.CASE:
      case DelphiLexer.DO:
      case DelphiLexer.ELSE:
      case DelphiLexer.EXCEPT:
      case DelphiLexer.FINALLY:
      case DelphiLexer.TkCaseItemSelector:
      case DelphiLexer.THEN:
      case DelphiLexer.TRY:
        return true;

      default:
        // Do nothing
    }

    return false;
  }

  private boolean isStatementTerminator(Tree node) {
    return isSemicolonAfterSingleStatement(node) || isBlockTerminatorAfterMissingSemicolon(node);
  }

  private boolean isSemicolonAfterSingleStatement(Tree node) {
    return node.getType() == DelphiLexer.SEMI && followsSingleStatement(node);
  }

  private boolean followsSingleStatement(Tree node) {
    int childIndex = node.getChildIndex();
    if (childIndex == 0) {
      return false;
    }

    Tree parent = node.getParent();
    int prevType = parent.getChild(childIndex - 1).getType();

    return prevType != DelphiLexer.BEGIN && prevType != DelphiLexer.END;
  }

  private boolean isBlockTerminatorAfterMissingSemicolon(Tree node) {
    int type = node.getType();

    if (type != DelphiLexer.END
        && type != DelphiLexer.UNTIL
        && type != DelphiLexer.EXCEPT
        && type != DelphiLexer.FINALLY) {
      return false;
    }

    int childIndex = node.getChildIndex();
    if (childIndex == 0) {
      return false;
    }

    int prevType = node.getParent().getChild(childIndex - 1).getType();

    return (prevType != DelphiLexer.SEMI
        && prevType != DelphiLexer.EXCEPT
        && prevType != DelphiLexer.FINALLY);
  }

  private boolean shouldSkip(DelphiPMDNode node) {
    int type = node.getType();

    return type != DelphiLexer.CONSTRUCTOR
        && type != DelphiLexer.DESTRUCTOR
        && type != DelphiLexer.FUNCTION
        && type != DelphiLexer.PROCEDURE;
  }

  private Tree findBeginNode(DelphiPMDNode parent) {
    for (int i = parent.getChildIndex() + 1; i < parent.getParent().getChildCount(); ++i) {
      Tree sibling = parent.getParent().getChild(i);
      int type = sibling.getType();

      if (type == DelphiLexer.BEGIN) {
        return sibling;
      }

      if (type != DelphiLexer.TkBlockDeclSection) {
        break;
      }
    }

    return null;
  }

  private String getMethodName(DelphiPMDNode node) {
    StringBuilder methodName = new StringBuilder();
    Tree nameNode = node.getFirstChildWithType(DelphiLexer.TkFunctionName);

    for (int c = 0; c < nameNode.getChildCount(); ++c) {
      methodName.append(nameNode.getChild(c).getText());
    }

    return methodName.toString();
  }
}
