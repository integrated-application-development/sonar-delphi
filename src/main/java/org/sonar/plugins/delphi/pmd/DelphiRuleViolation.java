/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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
package org.sonar.plugins.delphi.pmd;

import net.sourceforge.pmd.IRuleViolation;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;

import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.pmd.rules.DelphiRule;

/**
 * Delphi pmd rule violation
 */
public class DelphiRuleViolation implements IRuleViolation {

  private Rule rule;
  private String description;
  private String filename;

  private String className;
  private String methodName;
  private String packageName;
  private int beginLine;
  private int endLine;

  private int beginColumn;
  private int endColumn;

  private boolean isProcedureOrFunction(int type) {
    return type == DelphiLexer.PROCEDURE || type == DelphiLexer.FUNCTION;
  }

  /**
   * C-tor used in XPathRule, bacause we don't have node information
   * 
   * @param delphiRule
   *          DelphiLanguage rule
   * @param ctx
   *          Rule context
   * @param delphiClassName
   *          Class name
   * @param delphiMethodName
   *          Method name
   * @param delphiPackageName
   *          Package name
   * @param line
   *          Violation line number
   * @param column
   *          Violation column number
   * @param msg
   *          Violation message
   */
  public DelphiRuleViolation(DelphiRule delphiRule, RuleContext ctx, String delphiClassName, String delphiMethodName,
      String delphiPackageName, int line, int column, String msg) {
    rule = delphiRule;
    filename = ctx.getSourceCodeFile().getAbsolutePath();
    description = rule.getMessage();
    className = delphiClassName;
    methodName = delphiMethodName;
    packageName = delphiPackageName;
    beginLine = line;
    endLine = line;
    beginColumn = column;
    endColumn = column;
    description = msg;
  }

  /**
   * C-tor used as in PMD library
   * 
   * @param rule
   *          DelphiLanguage rule
   * @param ctx
   *          Rule context
   * @param node
   *          Violation node
   */
  public DelphiRuleViolation(DelphiRule rule, RuleContext ctx, DelphiPMDNode node) {
    this(rule, ctx, node, rule.getMessage());
  }

  /**
   * C-tor used as in PMD library
   * 
   * @param rule
   *          DelphiLanguage rule
   * @param ctx
   *          Rule context
   * @param node
   *          Violation node
   * @param message
   *          Violation message
   */
  public DelphiRuleViolation(DelphiRule rule, RuleContext ctx, DelphiPMDNode node, String message) {
    this.rule = rule;
    this.filename = ctx.getSourceCodeFile().getAbsolutePath();
    this.description = message;

    if (node != null) // there is node to analyse
    {
      // gets class name
      Tree classTypeNode = node.getAncestor(DelphiLexer.TkNewType);
      if (classTypeNode != null) {
        Tree classNameNode = classTypeNode.getChild(0);
        className = classNameNode.getText();
      } else {
        className = "";
      }

      // gets method node
      Tree methodNode = node.getAncestor(DelphiLexer.FUNCTION);
      if (methodNode == null) {
        methodNode = node.getAncestor(DelphiLexer.PROCEDURE);
      }

      if (methodNode == null) // look for method from begin...end statements
      {
        Tree currentNode = node;
        Tree beginNode = null;
        while ((beginNode = currentNode.getAncestor(DelphiLexer.BEGIN)) != null) {
          Tree parent = beginNode.getParent();
          currentNode = parent;
          int index = beginNode.getChildIndex();
          for (int lookBack = 1; lookBack <= 2; ++lookBack) {
            if (index - lookBack > -1 && isProcedureOrFunction(parent.getChild(index - lookBack).getType())) {
              methodNode = parent.getChild(index - lookBack);
              break;
            }
            if (methodNode != null) {
              break;
            }
          }
        }
      }

      // gets method name
      if (methodNode != null) {
        StringBuilder name = new StringBuilder();
        Tree nameNode = ((CommonTree) methodNode).getFirstChildWithType(DelphiLexer.TkFunctionName);
        for (int i = 0; i < nameNode.getChildCount(); ++i) {
          name.append(nameNode.getChild(i).getText());
        }
        methodName = name.toString();
        if (nameNode.getChildCount() > 1) {
          className = nameNode.getChild(0).getText(); // class name from function name
        }
      } else {
        methodName = "";
      }

      packageName = "";

      beginLine = node.getLine();
      endLine = beginLine;
      beginColumn = node.getCharPositionInLine();
      endColumn = beginColumn;

    } else // no node to analyse
    {
      className = "";
      methodName = "";
      packageName = "";
      filename = "";
    }
  }

  /**
   * {@inheritDoc}
   */

  public String getFilename() {
    return filename;
  }

  /**
   * {@inheritDoc}
   */

  public int getBeginLine() {
    return beginLine;
  }

  /**
   * {@inheritDoc}
   */

  public int getBeginColumn() {
    return beginColumn;
  }

  /**
   * {@inheritDoc}
   */

  public int getEndLine() {
    return endLine;
  }

  /**
   * {@inheritDoc}
   */

  public int getEndColumn() {
    return endColumn;
  }

  /**
   * {@inheritDoc}
   */

  public Rule getRule() {
    return rule;
  }

  /**
   * {@inheritDoc}
   */

  public String getDescription() {
    return description;
  }

  /**
   * {@inheritDoc}
   */

  public String getPackageName() {
    return packageName;
  }

  /**
   * {@inheritDoc}
   */

  public String getMethodName() {
    return methodName;
  }

  /**
   * {@inheritDoc}
   */

  public String getClassName() {
    return className;
  }

  /**
   * {@inheritDoc}
   */

  public boolean isSuppressed() {
    return false;
  }

  /**
   * {@inheritDoc}
   */

  public String getVariableName() {
    return "";
  }

}
