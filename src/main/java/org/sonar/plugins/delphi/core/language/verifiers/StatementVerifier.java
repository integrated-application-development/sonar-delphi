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
package org.sonar.plugins.delphi.core.language.verifiers;

import java.util.List;
import java.util.Stack;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.core.language.StatementInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiStatement;
import org.sonar.plugins.delphi.cpd.DelphiCpdTokenizer;

/**
 * Checks if a node can be transformed into a simple or complex statement
 */
public class StatementVerifier {

  private static final LexerMetrics[] STATEMENT_NODES = {LexerMetrics.IF, LexerMetrics.ELSE, LexerMetrics.WHILE,
    LexerMetrics.BREAK,
    LexerMetrics.CONTINUE};
  private static final int MIN_TOKENS_FOR_COMPLEX_STMT = 4;

  private Tree checkedNode = null;
  private boolean isComplex = false;
  private String lastStatementText = null;
  private Stack<Integer> statementIndex = new Stack<Integer>();
  private final DelphiProjectHelper delphiProjectHelper;

  public StatementVerifier(DelphiProjectHelper delphiProjectHelper) {
    this.delphiProjectHelper = delphiProjectHelper;
  }

  /**
   * Checks for statements
   * 
   * @param node Node to check
   */
  public boolean verify(Tree node) {
    isComplex = false;
    checkedNode = node;
    final boolean isSimple = isSimpleStatementNode(node);
    if (!isSimple) {
      this.isComplex = isComplexStatementNode(node);
    }
    return isSimple || isComplex;
  }

  /**
   * @return a Delphi statement
   * @throws StatementVerifierException we canCreateStatement returns false
   */
  public StatementInterface createStatement() {
    StatementInterface statement = new DelphiStatement(lastStatementText, checkedNode.getLine(),
      checkedNode.getCharPositionInLine(), delphiProjectHelper);
    statement.setComplexity(isComplex);
    return statement;
  }

  /**
   * @return True if a statement is a complex statement, false otherwise
   */
  public boolean isComplexStatement() {
    return isComplex;
  }

  private boolean isSimpleStatementNode(Tree node) {
    int nodeCode = node.getType();
    // special case for 'break' statement, since it went to
    // 'usedKeywordsAsNames' rule and now
    // it is recognized as TkIdentifier
    if (nodeCode == LexerMetrics.IDENT.toMetrics() && "break".equalsIgnoreCase(node.getText())) {
      return true;
    } else if (nodeCode == LexerMetrics.FOR.toMetrics()) {
      // special case for "for" statement
      statementIndex.pop();
      statementIndex.push(node.getChildIndex() + 1);
      lastStatementText = node.getText();
      return true;
    }
    for (LexerMetrics code : STATEMENT_NODES) {
      if (code.toMetrics() == nodeCode) {
        lastStatementText = node.getText();
        return true;
      }
    }
    return false;
  }

  private boolean isComplexStatementNode(Tree node) {
    // are we on a new block? (begin..end)
    if (isBeginEndNode(node)) {
      return false;
    }
    int childIndex = node.getChildIndex();
    if (childIndex <= statementIndex.peek()) {
      // optimization
      return false;
    }
    if (node.getType() != LexerMetrics.IDENT.toMetrics()) {
      // we are not on an variable id
      return false;
    }

    StringBuilder wholeLine = new StringBuilder(node.getText());
    CommonTree parent = (CommonTree) node.getParent();
    CommonTree assign = (CommonTree) parent.getChild(childIndex + 1);
    if (assign == null) {
      return false;
    }
    if (assign.getType() != LexerMetrics.ASSIGN.toMetrics()) {
      return false;
    }

    CommonTree actualNode = null;
    while ((actualNode = (CommonTree) parent.getChild(++childIndex)) != null) {
      isBeginEndNode(node);
      // while ; or ELSE
      if (actualNode.getType() == LexerMetrics.SEMI.toMetrics()
        || actualNode.getType() == LexerMetrics.ELSE.toMetrics()) {
        statementIndex.push(childIndex);
        break;
      }

      wholeLine.append(actualNode.getText());
    }

    // replace '..' with ' .. '
    String fixedSourceCode = wholeLine.toString().replaceAll("\\.\\.", " .. ");

    List<Token> tokens = new DelphiCpdTokenizer(delphiProjectHelper).tokenize(new String[] {fixedSourceCode});
    if (tokens.size() < MIN_TOKENS_FOR_COMPLEX_STMT) {
      // at least 4 tokens: id, :=, id, ;
      return false;
    }
    Token second = tokens.get(1);
    if (second.getType() == LexerMetrics.ASSIGN.toMetrics()) {
      lastStatementText = wholeLine.toString();
      return true;
    }

    return false;
  }

  private boolean isBlockNode(int code) {
    return code == LexerMetrics.BEGIN.toMetrics() || code == LexerMetrics.TRY.toMetrics()
      || code == LexerMetrics.CASE.toMetrics();
  }

  private boolean isBeginEndNode(Tree node) {
    if (isBlockNode(node.getType())) {
      statementIndex.push(-1);
      return true;
    } else if (node.getType() == LexerMetrics.END.toMetrics()) {
      statementIndex.pop();
      return true;
    }
    return false;
  }

}
