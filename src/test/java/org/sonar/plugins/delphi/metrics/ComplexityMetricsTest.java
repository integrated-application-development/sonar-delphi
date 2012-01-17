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
package org.sonar.plugins.delphi.metrics;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;
import org.sonar.plugins.delphi.antlr.analyzer.ASTAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisCacheResults;
import org.sonar.plugins.delphi.antlr.analyzer.DelphiASTAnalyzer;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class ComplexityMetricsTest {

  private static final String FILE_NAME = "/org/sonar/plugins/delphi/metrics/ComplexityMetricsTest.pas";

  @Test
  public void analyseTest() throws Exception {
    // init
    File testFile = DelphiUtils.getResource(FILE_NAME);
    CodeAnalysisCacheResults.resetCache();
    ASTAnalyzer analyzer = new DelphiASTAnalyzer();
    analyzer.analyze(new DelphiAST(testFile));

    // processing
    ComplexityMetrics metrics = new ComplexityMetrics(null);
    metrics.analyse(null, null, analyzer.getResults().getClasses(), analyzer.getResults().getFunctions(), null);
    String[] keys = { "ACCESSORS", "CLASS_COMPLEXITY", "CLASSES", "COMPLEXITY", "FUNCTIONS", "FUNCTION_COMPLEXITY", "PUBLIC_API",
        "STATEMENTS", "DEPTH_IN_TREE", "NUMBER_OF_CHILDREN", "RFC" };
    double[] values = { 2.0, 3.5, 2.0, 10.0, 4.0, 2.5, 5.0, 20.0, 2.0, 1.0, 3.0 };

    for (int i = 0; i < keys.length; ++i) {
      assertEquals(keys[i] + " failure ->", values[i], metrics.getMetric(keys[i]), 0.0);
    }

  }

}
