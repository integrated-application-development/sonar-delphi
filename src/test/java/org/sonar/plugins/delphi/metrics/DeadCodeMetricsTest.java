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
import static org.junit.Assert.assertFalse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.RecognitionException;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.analyzer.ASTAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.DelphiASTAnalyzer;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.core.DelphiFile;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.ClassPropertyInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.core.language.UnitInterface;
import org.sonar.plugins.delphi.core.language.impl.DelphiClass;
import org.sonar.plugins.delphi.core.language.impl.DelphiClassProperty;
import org.sonar.plugins.delphi.core.language.impl.DelphiFunction;
import org.sonar.plugins.delphi.core.language.impl.DelphiUnit;
import org.sonar.plugins.delphi.debug.DebugSensorContext;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class DeadCodeMetricsTest {

  private static final String TEST_FILE = "/org/sonar/plugins/delphi/metrics/DeadCodeMetricsTest.pas";
  private DeadCodeMetrics metrics;
  private List<UnitInterface> units;
  private List<ClassInterface> classes;
  private List<FunctionInterface> functions;

  @Before
  public void init() {
    functions = new ArrayList<FunctionInterface>();
    classes = new ArrayList<ClassInterface>();
    units = new ArrayList<UnitInterface>();

    FunctionInterface f1 = new DelphiFunction("function1");
    FunctionInterface f2 = new DelphiFunction("function2");
    FunctionInterface f3 = new DelphiFunction("function3");
    f1.addCalledFunction(f2);
    f2.addCalledFunction(f1);
    f3.setLine(321);

    ClassPropertyInterface p1 = new DelphiClassProperty();
    p1.setReadFunction(f1);
    p1.setWriteFunction(f2);

    ClassInterface c1 = new DelphiClass("class1");
    ClassInterface c2 = new DelphiClass("class2");
    c1.addFunction(f1);
    c2.addFunction(f2);
    c2.addFunction(f3);
    c2.addProperty(p1);

    UnitInterface u1 = new DelphiUnit("unit1");
    UnitInterface u2 = new DelphiUnit("unit2");
    UnitInterface u3 = new DelphiUnit("unit3");
    u1.setPath("unit1.dpr");
    u2.setPath("unit2.pas");
    u3.setPath("unit3.pas");

    u1.addIncludes("unit2");
    u2.addIncludes("unit1");
    u3.setLine(123);
    u1.addClass(c1);
    u2.addClass(c2);

    units.add(u1);
    units.add(u2);
    units.add(u3);

    metrics = new DeadCodeMetrics(null);
  }

  // @Test
  public void analyseTest() {
    DebugSensorContext context = new DebugSensorContext();
    metrics.analyse(null, context, classes, functions, units);
    metrics.save(new DelphiFile("unit3"), context);
    metrics.save(new DelphiFile("unit2"), context);

    assertEquals(2, context.getViolationsCount());
    int lines[] = { 123, 321 };
    for (int i = 0; i < context.getViolationsCount(); ++i) {
      assertEquals("Invalid unit line", lines[i], context.getViolation(i).getLineId().intValue());
    }

  }

  @Test
  public void analyseFileTest() throws IllegalStateException, IOException, RecognitionException {
    DebugSensorContext context = new DebugSensorContext();
    DelphiAST ast = new DelphiAST(DelphiUtils.getResource(TEST_FILE));
    ASTAnalyzer analyser = new DelphiASTAnalyzer();
    assertFalse("Grammar error", ast.isError());
    analyser.analyze(ast);
    metrics.analyse(null, context, analyser.getResults().getClasses(), analyser.getResults().getFunctions(), analyser.getResults()
        .getCachedUnitsAsList());

    DelphiFile resource = new DelphiFile("DeadCodeUnit", false);
    metrics.save(resource, context);

    assertEquals(3, context.getViolationsCount());
  }

}
