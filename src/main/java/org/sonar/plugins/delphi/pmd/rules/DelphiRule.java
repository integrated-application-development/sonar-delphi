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
package org.sonar.plugins.delphi.pmd.rules;

import java.util.Iterator;
import java.util.List;

import net.sourceforge.pmd.AbstractJavaRule;
import net.sourceforge.pmd.RuleContext;

import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.pmd.DelphiRuleViolation;

/**
 * Basic rule class, extend this class to make your own rules. Do NOT extend from AbstractRule.
 */
public class DelphiRule extends AbstractJavaRule {

  protected int lastLineParsed;

  public DelphiRule() {
  }
  
  /**
   * overload this method in derived class
   */
  public Object visit(DelphiPMDNode node, Object data) {
    return data;
  }

  /**
   * Visits all nodes in a file
   */

  @Override
  protected void visitAll(List acus, RuleContext ctx) {
    lastLineParsed = -1;
    init();
    for (Iterator i = acus.iterator(); i.hasNext();) {
      DelphiPMDNode node = (DelphiPMDNode) i.next();
      ASTTree ast = node.getASTTree();
      if (ast != null) {
        String codeLine = node.getASTTree().getFileSourceLine(node.getLine());
        if (codeLine.trim().endsWith("//NOSONAR") && node.getLine() + 1 > lastLineParsed) { // skip pmd analysis
          lastLineParsed = node.getLine() + 1;
        }
      }

      if (node.getLine() >= lastLineParsed) { // optimization and  / / NO SONAR line skip
        visit(node, ctx);
        lastLineParsed = node.getLine();
      }
    }
  }

  /**
   * Overload this method in derived class to initialize your rule instance with default values
   */
  protected void init() {
  }

  /**
   * Adds violation, get violation data from node
   * 
   * @param data
   *          Data
   * @param node
   *          Node
   */
  protected void addViolation(Object data, DelphiPMDNode node) {
    RuleContext ctx = (RuleContext) data;
    ctx.getReport().addRuleViolation(new DelphiRuleViolation(this, ctx, node));
  }

  /**
   * Adds violation, get violation data from node
   * 
   * @param data
   *          Data
   * @param node
   *          Node
   * @param msg
   *          Violation message
   */
  protected void addViolation(Object data, DelphiPMDNode node, String msg) {
    RuleContext ctx = (RuleContext) data;
    ctx.getReport().addRuleViolation(new DelphiRuleViolation(this, ctx, node, msg));
  }

  /**
   * Adds violation, used in XPathRule
   * 
   * @param data
   *          Data
   * @param violation
   *          Violation
   */
  protected void addViolation(Object data, DelphiRuleViolation violation) {
    RuleContext ctx = (RuleContext) data;
    ctx.getReport().addRuleViolation(violation);
  }

}
