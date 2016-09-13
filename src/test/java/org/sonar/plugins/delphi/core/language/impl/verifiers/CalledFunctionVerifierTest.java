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
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeNode;
import org.sonar.plugins.delphi.antlr.analyzer.CodeTree;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;
import org.sonar.plugins.delphi.antlr.analyzer.impl.operations.AdvanceNodeOperation;
import org.sonar.plugins.delphi.antlr.analyzer.impl.operations.AdvanceToNodeOperation;
import org.sonar.plugins.delphi.antlr.analyzer.impl.operations.NodeOperation;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiFunction;
import org.sonar.plugins.delphi.core.language.impl.DelphiUnit;
import org.sonar.plugins.delphi.core.language.verifiers.CalledFunctionVerifier;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class CalledFunctionVerifierTest {

  private static final String TEST_FILE = "/org/sonar/plugins/delphi/verifier/CalledFunctionsTest.pas";
  private static final int BEGIN_STMTS_IN_TEST_FILE = 2;
  CalledFunctionVerifier verifier;
  CodeAnalysisResults results;
  CodeTree code;

  @Before
  public void setup() throws IOException, RecognitionException {
    results = new CodeAnalysisResults();
    verifier = new CalledFunctionVerifier(results);

    UnitInterface testUnit = new DelphiUnit("testUnit");
    testUnit.setPath("testUnit.pas");
    testUnit.addFunction(new DelphiFunction("myprocedure"));
    testUnit.addFunction(new DelphiFunction("mysecondprocedure"));
    testUnit.addIncludes("unitA");
    testUnit.addIncludes("unitB");

    UnitInterface unitA = new DelphiUnit("unitA");
    unitA.setPath("unitA.pas");
    unitA.addFunction(new DelphiFunction("unita_procedure"));

    UnitInterface unitB = new DelphiUnit("unitB");
    unitB.setPath("unitB.pas");
    unitB.addFunction(new DelphiFunction("unitb_procedure"));

    results.cacheUnit(unitA);
    results.cacheUnit(unitB);
    results.cacheUnit(testUnit);
    results.setActiveUnit(testUnit);

    File file = DelphiUtils.getResource(TEST_FILE);
    ASTTree ast = new DelphiAST(file);
    code = new CodeTree(new CodeNode<ASTTree>(ast), new CodeNode<Tree>(ast.getChild(0)));
  }

  @Test
  public void verifyTest() {
    final boolean unresolved[] = {false, false, false, true};
    final String names[] = {"mySecondProcedure", "unitB_Procedure", "unitA_Procedure", "unitC_Procedure"};

    NodeOperation advanceToOperation = new AdvanceToNodeOperation(LexerMetrics.FUNCTION_BODY);
    NodeOperation advanceOperation = new AdvanceNodeOperation();
    CodeNode<Tree> currentNode = code.getCurrentCodeNode();

    int index = 0;
    for (int i = 0; i < BEGIN_STMTS_IN_TEST_FILE; ++i) {
      currentNode = advanceToOperation.execute(currentNode.getNode());
      code.setCurrentNode(currentNode);

      CodeNode<Tree> atNode = currentNode;
      boolean endNode = false;
      while (atNode.isValid() && !endNode) {
        if (verifier.verify(atNode.getNode())) {
          FunctionInterface calledFunction = verifier.fetchCalledFunction();
          assertEquals(unresolved[index], verifier.isUnresolvedFunctionCall());
          assertEquals(names[index].toLowerCase(), calledFunction.getName());
          ++index;
        }

        atNode = advanceOperation.execute(atNode.getNode());
        endNode = (atNode.getNode().getType() == LexerMetrics.END.toMetrics());
      }
    }

  }

}
