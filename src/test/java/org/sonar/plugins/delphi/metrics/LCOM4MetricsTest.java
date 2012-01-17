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

import org.junit.Test;
import org.sonar.plugins.delphi.antlr.analyzer.ASTAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.DelphiASTAnalyzer;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class LCOM4MetricsTest {

  private final static String FILE_NAME = "/org/sonar/plugins/delphi/metrics/LCOM4MetricsTest.pas";

  @Test
  public void analyseTest() throws Exception {
    DelphiAST ast = new DelphiAST(DelphiUtils.getResource(FILE_NAME));
    ASTAnalyzer analyzer = new DelphiASTAnalyzer();
    analyzer.analyze(ast);

    LCOM4Metrics metric = new LCOM4Metrics(null);
    metric.analyse(null, null, analyzer.getResults().getClasses(), analyzer.getResults().getFunctions(), null);

    double LOC4 = metric.getMetric("loc4");
    assertEquals(3.0, LOC4, 0.0);
  }
}
