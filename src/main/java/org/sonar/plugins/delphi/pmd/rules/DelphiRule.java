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

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRule;
import net.sourceforge.pmd.lang.rule.ImmutableLanguage;
import net.sourceforge.pmd.properties.IntegerProperty;
import net.sourceforge.pmd.properties.StringProperty;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.pmd.DelphiLanguageModule;
import org.sonar.plugins.delphi.pmd.DelphiParserVisitor;
import org.sonar.plugins.delphi.pmd.DelphiRuleViolation;

/**
 * Basic rule class, extend this class to make your own rules. Do NOT extend from AbstractRule.
 */
public class DelphiRule extends AbstractRule implements DelphiParserVisitor, ImmutableLanguage {

  protected int lastLineParsed;

  private int currentVisibility;

  private boolean inImplementationSection;

  protected static final IntegerProperty LIMIT = new IntegerProperty("limit",
      "The max limit.", 1, 150,
      1, 1.0f);
  protected static final IntegerProperty THRESHOLD = new IntegerProperty("Threshold",
      "Threshold", 1,
      100, 10, 1.0f);
  protected static final StringProperty START_AST = new StringProperty("start",
      "The AST node to start from", "", 1.0f);
  protected static final StringProperty END_AST = new StringProperty("end",
      "The AST node to stop the search", "", 1.0f);
  protected static final StringProperty LOOK_FOR = new StringProperty("lookFor",
      "What nodes look for",
      "", 1.0f);
  protected static final StringProperty BASEEFFORT = new StringProperty("baseEffort",
      "The base effort to correct", "", 1.0f);

  public DelphiRule() {
    super.setLanguage(LanguageRegistry.getLanguage(DelphiLanguageModule.LANGUAGE_NAME));
    definePropertyDescriptor(LIMIT);
    definePropertyDescriptor(THRESHOLD);
    definePropertyDescriptor(START_AST);
    definePropertyDescriptor(END_AST);
    definePropertyDescriptor(LOOK_FOR);
    definePropertyDescriptor(BASEEFFORT);
  }

  /**
   * overload this method in derived class
   *
   * @param node the current node
   * @param ctx the ruleContext to store the violations
   */
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    // do nothing
  }

  /**
   * Visits all nodes in a file
   */
  @Override
  public Object visit(DelphiPMDNode node, Object data) {
    return null;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void apply(List<? extends Node> nodes, RuleContext ctx) {
    visitAll(nodes, ctx);
  }

  protected void visitAll(List<? extends Node> acus, RuleContext ctx) {
    lastLineParsed = -1;
    currentVisibility = DelphiLexer.PUBLISHED;
    inImplementationSection = false;
    init();
    for (Node acu : acus) {
      DelphiPMDNode node = (DelphiPMDNode) acu;
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
   * Overload this method in derived class to initialize your rule instance with default values
   */
  protected void init() {
    // Used to overload default values in a rule, does not have to be used
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

  boolean isImplementationSection() {
    return inImplementationSection;
  }

}
