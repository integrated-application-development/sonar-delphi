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
import java.util.Set;
import java.util.stream.Collectors;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.lang.LanguageRegistry;
import net.sourceforge.pmd.lang.ast.Node;
import net.sourceforge.pmd.lang.rule.AbstractRule;
import net.sourceforge.pmd.lang.rule.ImmutableLanguage;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import net.sourceforge.pmd.properties.constraints.NumericConstraints;
import org.antlr.runtime.Token;
import org.sonar.api.rule.RuleScope;
import org.sonar.api.rules.RuleType;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.DelphiNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.pmd.DelphiLanguageModule;
import org.sonar.plugins.delphi.pmd.DelphiParserVisitor;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.pmd.DelphiRuleViolationBuilder;

/** Basic rule class, extend this class to make your own rules. Do NOT extend from AbstractRule. */
public class DelphiRule extends AbstractRule implements DelphiParserVisitor, ImmutableLanguage {
  private int currentVisibility;
  private boolean inImplementationSection;
  private Set<Integer> suppressions;

  protected static final PropertyDescriptor<Integer> LIMIT =
      PropertyFactory.intProperty("limit")
          .desc("The max limit.")
          .require(NumericConstraints.inRange(1, 150))
          .defaultValue(1)
          .build();

  public static final PropertyDescriptor<String> BASE_EFFORT =
      PropertyFactory.stringProperty(DelphiPmdConstants.BASE_EFFORT)
          .desc("The base effort to correct")
          .defaultValue("")
          .build();

  public static final PropertyDescriptor<String> SCOPE =
      PropertyFactory.stringProperty(DelphiPmdConstants.SCOPE)
          .desc("The type of code this rule should apply to")
          .defaultValue(RuleScope.ALL.name())
          .build();

  public static final PropertyDescriptor<Boolean> TEMPLATE =
      PropertyFactory.booleanProperty(DelphiPmdConstants.TEMPLATE)
          .desc("Whether the rule is a template")
          .defaultValue(false)
          .build();

  public static final PropertyDescriptor<String> TYPE =
      PropertyFactory.stringProperty(DelphiPmdConstants.TYPE)
          .desc("Rule type: Options are 'CODE_SMELL', 'BUG', 'VULNERABILITY' or 'SECURITY_HOTSPOT'")
          .defaultValue(RuleType.CODE_SMELL.name())
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

    definePropertyDescriptor(BASE_EFFORT);
    definePropertyDescriptor(LIMIT);
    definePropertyDescriptor(SCOPE);
    definePropertyDescriptor(TEMPLATE);
    definePropertyDescriptor(TYPE);
    definePropertyDescriptor(START_AST);
    definePropertyDescriptor(END_AST);
  }

  @Override
  public void visit(DelphiNode node, RuleContext ctx) {
    // Do nothing
  }

  @Override
  public void visitFile(DelphiAST ast, RuleContext ctx) {
    // Do nothing
  }

  @Override
  public void visitComment(Token comment, RuleContext ctx) {
    // Do nothing
  }

  /** {@inheritDoc} */
  @Override
  public void apply(List<? extends Node> nodes, RuleContext ctx) {
    if (getProperty(TEMPLATE) || nodes.isEmpty()) {
      return;
    }

    DelphiNode firstNode = (DelphiNode) nodes.get(0);
    DelphiAST ast = firstNode.getASTTree();

    updateSuppressions(ast);

    visitNodes(nodes, ctx);
    visitFile(ast, ctx);
    visitComments(ast, ctx);
  }

  private void updateSuppressions(DelphiAST ast) {
    suppressions =
        ast.getComments().stream()
            .filter(comment -> comment.getText().contains("NOSONAR"))
            .map(Token::getLine)
            .collect(Collectors.toSet());
  }

  private void visitNodes(List<? extends Node> nodes, RuleContext ctx) {
    currentVisibility = DelphiLexer.PUBLISHED;
    inImplementationSection = false;

    for (Node acu : nodes) {
      DelphiNode node = (DelphiNode) acu;

      updateVisibility(node);
      updateIsImplementation(node);
      visit(node, ctx);
    }
  }

  private void visitComments(DelphiAST ast, RuleContext ctx) {
    for (Token comment : ast.getComments()) {
      visitComment(comment, ctx);
    }
  }

  /**
   * Adds violation to pmd report for a DelphiNode
   *
   * @param ctx RuleContext
   * @param node Node
   */
  protected void addViolation(RuleContext ctx, DelphiNode node) {
    newViolation(ctx).fileLocation(node).logicalLocation(node).save();
  }

  /**
   * Adds violation to pmd report for a DelphiNode (with override message)
   *
   * @param ctx RuleContext
   * @param node Violation node
   * @param msg Violation message
   */
  protected void addViolation(RuleContext ctx, DelphiNode node, String msg) {
    newViolation(ctx).fileLocation(node).logicalLocation(node).message(msg).save();
  }

  /**
   * Adds violation to pmd report for a token
   *
   * @param ctx RuleContext
   * @param token Violation token
   */
  protected void addViolation(RuleContext ctx, Token token) {
    newViolation(ctx).fileLocation(token).save();
  }

  /**
   * Adds violation to pmd report for a token (with override message)
   *
   * @param ctx RuleContext
   * @param token Violation token
   * @param msg Violation message
   */
  protected void addViolation(RuleContext ctx, Token token, String msg) {
    newViolation(ctx).fileLocation(token).message(msg).save();
  }

  protected DelphiRuleViolationBuilder newViolation(RuleContext ctx) {
    return DelphiRuleViolationBuilder.newViolation(this, ctx);
  }

  private void updateVisibility(DelphiNode node) {
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

  private void updateIsImplementation(DelphiNode node) {
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

  public Set<Integer> getSuppressions() {
    return suppressions;
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
