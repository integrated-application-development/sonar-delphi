/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.collect.Iterables;
import java.io.File;
import net.sourceforge.pmd.Rule;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.RuleViolation;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.ast.node.FileHeaderNode;
import org.sonar.plugins.delphi.antlr.ast.node.IdentifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.QualifiedNameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.UnitDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.token.DelphiToken;
import org.sonar.plugins.delphi.antlr.ast.token.IncludeToken;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.file.DelphiFile;
import org.sonar.plugins.delphi.pmd.rules.AbstractDelphiRule;
import org.sonar.plugins.delphi.pmd.violation.DelphiRuleViolation;
import org.sonar.plugins.delphi.pmd.violation.DelphiRuleViolationFactory;

class DelphiRuleViolationFactoryTest {
  private static final File SOURCE_FILE = new File("code.pas");
  private static final String MESSAGE = "MESSAGE";
  private static final int LINE = 1;
  private static final int START_COLUMN = 1;
  private static final int END_COLUMN = 2;
  private static final int OVERRIDE_START_LINE = 2;
  private static final int OVERRIDE_END_LINE = 3;
  private static final int INCLUDE_LINE = 3;
  private static final int INCLUDE_START_COLUMN = 3;
  private static final int INCLUDE_END_COLUMN = 4;
  private DelphiRuleViolationFactory factory;
  private RuleContext context;
  private Rule rule;

  @BeforeEach
  void setup() {
    factory = new DelphiRuleViolationFactory();
    context = new RuleContext();
    context.setSourceCodeFile(SOURCE_FILE);
    rule = new AbstractDelphiRule() {};
  }

  @Test
  void testCreateRuleViolation() {
    factory.addViolation(context, rule, createNode(), MESSAGE, Arrays.array());

    DelphiRuleViolation violation = getViolation();
    assertThat(violation.getRule()).isEqualTo(rule);
    assertThat(violation.getFilename()).isEqualTo(SOURCE_FILE.getAbsolutePath());
    assertThat(violation.getBeginLine()).isEqualTo(LINE);
    assertThat(violation.getEndLine()).isEqualTo(LINE);
    assertThat(violation.getBeginColumn()).isEqualTo(START_COLUMN);
    assertThat(violation.getEndColumn()).isEqualTo(END_COLUMN);
  }

  @Test
  void testCreateRuleViolationWithLineOverride() {
    factory.addViolation(
        context,
        rule,
        createNode(),
        MESSAGE,
        OVERRIDE_START_LINE,
        OVERRIDE_END_LINE,
        Arrays.array());

    DelphiRuleViolation violation = getViolation();
    assertThat(violation.getRule()).isEqualTo(rule);
    assertThat(violation.getFilename()).isEqualTo(SOURCE_FILE.getAbsolutePath());
    assertThat(violation.getBeginLine()).isEqualTo(OVERRIDE_START_LINE);
    assertThat(violation.getEndLine()).isEqualTo(OVERRIDE_END_LINE);
    assertThat(violation.getBeginColumn()).isEqualTo(FilePosition.UNDEFINED_COLUMN);
    assertThat(violation.getEndColumn()).isEqualTo(FilePosition.UNDEFINED_COLUMN);
  }

  @Test
  void testIncludeToken() {
    Token token = mockToken(LINE, START_COLUMN);
    DelphiToken insertionToken = new DelphiToken(mockToken(INCLUDE_LINE, INCLUDE_START_COLUMN));
    IncludeToken includeToken = new IncludeToken(token, insertionToken);

    factory.addViolation(context, rule, createNode(includeToken), MESSAGE, Arrays.array());

    DelphiRuleViolation violation = getViolation();
    assertThat(violation.getRule()).isEqualTo(rule);
    assertThat(violation.getFilename()).isEqualTo(SOURCE_FILE.getAbsolutePath());
    assertThat(violation.getBeginLine()).isEqualTo(INCLUDE_LINE);
    assertThat(violation.getEndLine()).isEqualTo(INCLUDE_LINE);
    assertThat(violation.getBeginColumn()).isEqualTo(INCLUDE_START_COLUMN);
    assertThat(violation.getEndColumn()).isEqualTo(INCLUDE_END_COLUMN);
  }

  private DelphiRuleViolation getViolation() {
    assertThat(context.getReport()).hasSize(1);
    RuleViolation violation = Iterables.getLast(context.getReport());
    assertThat(violation).isInstanceOf(DelphiRuleViolation.class);
    return (DelphiRuleViolation) violation;
  }

  private static DelphiNode createNode() {
    return createNode(mockToken(LINE, START_COLUMN));
  }

  private static DelphiNode createNode(Token token) {
    DelphiNode result =
        new DelphiNode(token) {
          @Override
          public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
            return null;
          }
        };
    result.jjtSetParent(createRootNode());
    return result;
  }

  private static Token mockToken(int line, int column) {
    Token token = mock(CommonToken.class);
    when(token.getLine()).thenReturn(line);
    when(token.getCharPositionInLine()).thenReturn(column);
    when(token.getText()).thenReturn(" ");
    return token;
  }

  private static DelphiAST createRootNode() {
    DelphiAST ast = new DelphiAST(mock(DelphiFile.class), null);
    FileHeaderNode header = new UnitDeclarationNode(Token.INVALID_TOKEN);
    ast.jjtAddChild(header);
    QualifiedNameDeclarationNode nameNode = new QualifiedNameDeclarationNode(Token.INVALID_TOKEN);
    nameNode.jjtAddChild(new IdentifierNode(Token.INVALID_TOKEN));
    header.jjtAddChild(nameNode);
    return ast;
  }
}
