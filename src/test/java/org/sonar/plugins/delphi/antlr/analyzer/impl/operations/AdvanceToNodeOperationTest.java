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
package org.sonar.plugins.delphi.antlr.analyzer.impl.operations;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.analyzer.LexerMetrics;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class AdvanceToNodeOperationTest {

  private static final int EXPECTED_EXECUTIONS_COUNT = 3;

  private Tree root;
  private NodeOperation operation;

  @Before
  public void setup() {
    CommonToken rootToken = new CommonToken(LexerMetrics.UNIT.toMetrics(), "unit");
    CommonToken childToken = new CommonToken(LexerMetrics.FUNCTION.toMetrics(), "function");

    Tree branch = new CommonTree(rootToken);
    branch.addChild(new CommonTree(childToken));
    branch.addChild(new CommonTree(childToken));

    root = new CommonTree(new CommonToken(0));
    root.addChild(branch);

    operation = new AdvanceToNodeOperation(Arrays.asList(LexerMetrics.UNIT, LexerMetrics.FUNCTION));
  }

  @Test
  public void executeTest() {

    int index = 0;
    int expected[] = {LexerMetrics.UNIT.toMetrics(), LexerMetrics.FUNCTION.toMetrics(),
      LexerMetrics.FUNCTION.toMetrics()};

    int executionsCount = 0;

    Tree currentNode = root;
    while (currentNode != null) {
      try {
        currentNode = operation.execute(currentNode).getNode();
        assertEquals(expected[index++], currentNode.getType());
        ++executionsCount;
      } catch (IllegalStateException e) {
        currentNode = null;
      }
    }

    assertEquals(EXPECTED_EXECUTIONS_COUNT, executionsCount);
  }

}
