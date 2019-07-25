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

import com.qualinsight.plugins.sonarqube.smell.api.annotation.Smell;
import com.qualinsight.plugins.sonarqube.smell.api.model.SmellType;
import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRule;
import net.sourceforge.pmd.lang.rule.ImmutableLanguage;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.properties.constraints.NumericConstraints;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.pmd.DelphiLanguageModule;
import org.sonar.plugins.delphi.pmd.DelphiParserVisitor;
import org.sonar.plugins.delphi.pmd.DelphiRuleViolationBuilder;

/** Basic rule class, extend this class to make your own rules. Do NOT extend from AbstractRule. */
public class DelphiRule extends AbstractRule implements DelphiParserVisitor, ImmutableLanguage {

  protected int skipToLine;
  private int currentVisibility;
  private boolean inImplementationSection;

  protected static final PropertyDescriptor<Integer> LIMIT =
      PropertyFactory.intProperty("limit")
          .desc("The max limit.")
          .require(NumericConstraints.inRange(1, 150))
          .defaultValue(1)
          .build();

  private static final PropertyDescriptor<String> BASE_EFFORT =
      PropertyFactory.stringProperty("baseEffort")
          .desc("The base effort to correct")
          .defaultValue("")
          .build();

  protected static final PropertyDescriptor<String> START_AST =
      PropertyFactory.stringProperty("start")
          .desc("The AST node to start from")
          .defaultValue("")
          .build();

  protected static final PropertyDescriptor<String> END_AST =
      PropertyFactory.stringProperty("end")
          .desc("The AST node to stop the search")
          .defaultValue("")
          .build();

  public DelphiRule() {
    setLanguage(LanguageRegistry.getLanguage(DelphiLanguageModule.LANGUAGE_NAME));

    definePropertyDescriptor(LIMIT);
    definePropertyDescriptor(START_AST);
    definePropertyDescriptor(END_AST);
    definePropertyDescriptor(BASE_EFFORT);
  }

  /**
   * Visits all nodes in a file overload this method in derived class
   *
   * @param node the current node
   * @param ctx the ruleContext to store the violations
   */
  protected void visit(DelphiPMDNode node, RuleContext ctx) {
    // do nothing
  }

  /** Visits all nodes in a file */
  @Override
  public void visit(DelphiPMDNode node, Object data) {
    // do nothing
  }

  /** {@inheritDoc} */
  @Override
  public void apply(List<? extends Node> nodes, RuleContext ctx) {
    visitAll(nodes, ctx);
  }

  @Smell(
      minutes = 60,
      reason =
          "The //NOSONAR line skip is poorly implemented."
              + "We completely skip visiting any nodes on that line, which could lead to surprising"
              + "behavior when a rule traverses nodes on multiple lines."
              + "We're also doing a String.endsWith check on every line in the codebase."
              + "That probably isn't performing very well.",
      type = SmellType.BAD_DESIGN)
  protected void visitAll(List<? extends Node> acus, RuleContext ctx) {
    skipToLine = -1;
    currentVisibility = DelphiLexer.PUBLISHED;
    inImplementationSection = false;

    init();

    for (Node acu : acus) {
      DelphiPMDNode node = (DelphiPMDNode) acu;
      ASTTree ast = node.getASTTree();
      int nodeLine = node.getLine();

      if (ast != null && nodeLine > skipToLine) {
        String codeLine = node.getASTTree().getFileSourceLine(nodeLine);
        // skip pmd analysis
        if (codeLine.trim().endsWith("//NOSONAR")) {
          skipToLine = nodeLine + 1;
        }
      }

      if (nodeLine >= skipToLine) {
        updateVisibility(node);
        updateIsImplementation(node);
        visit(node, ctx);
      }
    }
  }

  /** Overload this method in derived class to initialize your rule instance with default values */
  protected void init() {
    // Used to overload default values in a rule, does not have to be used
  }

  /**
   * Adds violation to pmd report
   *
   * @param ctx RuleContext
   * @param node Node
   */
  protected void addViolation(RuleContext ctx, DelphiPMDNode node) {
    newViolation(ctx).fileLocation(node).logicalLocation(node).save();
  }

  /**
   * Adds violation to pmd report with override message
   *
   * @param ctx RuleContext
   * @param node Node
   * @param msg Violation message
   */
  protected void addViolation(RuleContext ctx, DelphiPMDNode node, String msg) {
    newViolation(ctx).fileLocation(node).logicalLocation(node).message(msg).save();
  }

  protected DelphiRuleViolationBuilder newViolation(RuleContext ctx) {
    return DelphiRuleViolationBuilder.newViolation(this, ctx);
  }

  private void updateVisibility(DelphiPMDNode node) {
    switch (node.getType()) {
      case DelphiLexer.PRIVATE:
      case DelphiLexer.PROTECTED:
      case DelphiLexer.PUBLIC:
      case DelphiLexer.PUBLISHED:
        currentVisibility = node.getType();
        break;

      case DelphiLexer.TkNewType:
        currentVisibility = DelphiLexer.PUBLISHED;
        break;

      default:
    }
  }

  private void updateIsImplementation(DelphiPMDNode node) {
    if (!inImplementationSection) {
      inImplementationSection = node.getType() == DelphiLexer.IMPLEMENTATION;
    }
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

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return super.hashCode();
  }
}
