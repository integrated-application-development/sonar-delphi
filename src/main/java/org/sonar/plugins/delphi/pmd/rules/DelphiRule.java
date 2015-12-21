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

import java.util.Iterator;
import java.util.List;
import net.sourceforge.pmd.AbstractJavaRule;
import net.sourceforge.pmd.PropertyDescriptor;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.IntegerProperty;
import net.sourceforge.pmd.properties.StringProperty;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.pmd.DelphiRuleViolation;

/**
 * Basic rule class, extend this class to make your own rules. Do NOT extend
 * from AbstractRule.
 */
public class DelphiRule extends AbstractJavaRule {

  protected int lastLineParsed;

  private int currentVisibility;

  private boolean inImplementationSection = false;

  public static final PropertyDescriptor LIMIT = new IntegerProperty("limit", "The max limit.", 1, 1.0f);
  public static final PropertyDescriptor START = new StringProperty("start", "The AST node to start from", "", 1.0f);
  public static final PropertyDescriptor END = new StringProperty("end", "The AST node to stop the search", "", 1.0f);
  public static final PropertyDescriptor LOOK_FOR = new StringProperty("lookFor", "What nodes look for", "", 1.0f);

  public DelphiRule() {
  }

  /**
   * overload this method in derived class
   */
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    // do nothing
  }

  /**
   * Visits all nodes in a file
   */

  @Override
  protected void visitAll(@SuppressWarnings("rawtypes") List acus, RuleContext ctx) {
    lastLineParsed = -1;
    currentVisibility = DelphiLexer.PUBLISHED;
    init();
    for (Iterator<?> i = acus.iterator(); i.hasNext();) {
      DelphiPMDNode node = (DelphiPMDNode) i.next();
      ASTTree ast = node.getASTTree();
      if (ast != null) {
        String codeLine = node.getASTTree().getFileSourceLine(node.getLine());
        // skip pmd analysis
        if (codeLine.trim().endsWith("//NOSONAR") && node.getLine() + 1 > lastLineParsed) {
          lastLineParsed = node.getLine() + 1;
        }
      }

      // optimization and //NOSONAR line skip
      if (node.getLine() >= lastLineParsed) {
        updateVisibility(node);
        if (!inImplementationSection) {
          inImplementationSection = node.getType() == DelphiLexer.IMPLEMENTATION;
        }
        visit(node, ctx);
        lastLineParsed = node.getLine();
      }
    }
  }

  /**
   * Overload this method in derived class to initialize your rule instance
   * with default values
   */
  protected void init() {
  }

  /**
   * Adds violation, get violation data from node
   * 
   * @param ctx RuleContext
   * @param node Node
   */
  protected void addViolation(RuleContext ctx, DelphiPMDNode node) {
    ctx.getReport().addRuleViolation(new DelphiRuleViolation(this, ctx, node));
  }

  /**
   * Adds violation, get violation data from node
   * 
   * @param ctx RuleContext
   * @param node Node
   * @param msg Violation message
   */
  protected void addViolation(RuleContext ctx, DelphiPMDNode node, String msg) {
    ctx.getReport().addRuleViolation(new DelphiRuleViolation(this, ctx, node, msg));
  }

  /**
   * Adds violation, used in XPathRule
   * 
   * @param ctx RuleContext
   * @param violation Violation
   */
  protected void addViolation(RuleContext ctx, DelphiRuleViolation violation) {
    ctx.getReport().addRuleViolation(violation);
  }

  private void updateVisibility(DelphiPMDNode node) {
    switch (node.getType()) {
      case DelphiLexer.PRIVATE:
      case DelphiLexer.PROTECTED:
      case DelphiLexer.PUBLIC:
      case DelphiLexer.PUBLISHED:
        currentVisibility = node.getType();
    }
  }

  public int getLastLineParsed() {
    return lastLineParsed;
  }

  protected boolean isProtected() {
    return currentVisibility == DelphiLexer.PROTECTED;
  }

  protected boolean isPrivate() {
    return currentVisibility == DelphiLexer.PRIVATE;
  }

  protected boolean isPublished() {
    return currentVisibility == DelphiLexer.PUBLISHED;
  }

  protected boolean isInterfaceSection() {
    return !isImplementationSection();
  }

  public boolean isImplementationSection() {
    return inImplementationSection;
  }
}
