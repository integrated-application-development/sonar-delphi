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
package org.sonar.plugins.delphi.core.language.impl.verifiers;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.tree.Tree;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.DelphiTestUtils;
import org.sonar.plugins.delphi.antlr.analyzer.CodeNode;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.antlr.analyzer.impl.operations.AdvanceNodeOperation;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.core.language.StatementInterface;
import org.sonar.plugins.delphi.core.language.verifiers.StatementVerifier;
import org.sonar.plugins.delphi.core.language.verifiers.StatementVerifierException;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class StatementVerifierTest {

  private static final int FILE_COMPLEX_STMT_COUNT = 11;
  private static final int FILE_STATEMENT_LINES[] = {27, 34, 41, 44, 50, 50, 51, 51, 52, 52, 59, 60, 61, 63, 63, 64,
    64, 70, 70, 71};
  private static final String FILE_NAME = "/org/sonar/plugins/delphi/metrics/ComplexityMetricsTest.pas";

  private StatementVerifier verifier;
  private ASTTree ast;
  private CodeTree codeTree;

  @Before
  public void setup() throws IOException, RecognitionException {
    File astFile = DelphiUtils.getResource(FILE_NAME);
    ast = new DelphiAST(astFile);
    codeTree = new CodeTree(new CodeNode<ASTTree>(ast), new CodeNode<Tree>(ast.getChild(0)));
    verifier = new StatementVerifier(DelphiTestUtils.mockProjectHelper());
  }

  @Test
  public void createStatementTest() throws StatementVerifierException {
    int statementCount = 0;
    int complexStatementsCount = 0;

    AdvanceNodeOperation operation = new AdvanceNodeOperation();
    CodeNode<Tree> currentNode = codeTree.getCurrentCodeNode();
    int lastLineParsed = -1;
    while (currentNode.isValid()) {

      if (currentNode.getNode().getType() == LexerMetrics.FUNCTION_BODY.toMetrics()
        && lastLineParsed <= currentNode.getNode().getLine()) {
        int beginCount = 1;
        CodeNode<Tree> atNode = currentNode;
        do {
          if (verifier.verify(atNode.getNode())) {

            StatementInterface statement = verifier.createStatement();
            assertEquals(FILE_STATEMENT_LINES[statementCount], statement.getLine());
            ++statementCount;

            if (verifier.isComplexStatement()) {
              ++complexStatementsCount;
            }
          }

          atNode = operation.execute(atNode.getNode());
          lastLineParsed = atNode.getNode().getLine();

          if (atNode.getNode().getType() == LexerMetrics.END.toMetrics()) {
            --beginCount;
          } else if (atNode.getNode().getType() == LexerMetrics.BEGIN.toMetrics()) {
            ++beginCount;
          }

        } while (atNode.isValid() && beginCount > 0);
      }
      currentNode = operation.execute(currentNode.getNode());
      codeTree.setCurrentNode(currentNode);
    }

    assertEquals(FILE_STATEMENT_LINES.length, statementCount);
    assertEquals(FILE_COMPLEX_STMT_COUNT, complexStatementsCount);
  }

}
