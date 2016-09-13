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
package org.sonar.plugins.delphi.antlr;

import org.antlr.runtime.RecognitionException;
import org.junit.BeforeClass;
import org.junit.Test;
import org.sonar.plugins.delphi.DelphiTestUtils;
import org.sonar.plugins.delphi.antlr.analyzer.ASTAnalyzer;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisCacheResults;
import org.sonar.plugins.delphi.antlr.analyzer.CodeAnalysisResults;
import org.sonar.plugins.delphi.antlr.analyzer.DelphiASTAnalyzer;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.core.language.ClassFieldInterface;
import org.sonar.plugins.delphi.core.language.ClassInterface;
import org.sonar.plugins.delphi.core.language.FunctionInterface;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class DelphiASTAnalyzerTest {

  static private int fileComplexity = 0;
  static private ASTAnalyzer analyser = null;
  private static final String FILE_NAME = "/org/sonar/plugins/delphi/metrics/FunctionMetricsTest.pas";

  @BeforeClass
  public static void setUp() throws Exception {
    fileComplexity = 0;
    CodeAnalysisCacheResults.resetCache();
    analyser = new DelphiASTAnalyzer(DelphiTestUtils.mockProjectHelper());
  }

  @Test
  public void analyseTest() throws IOException, RecognitionException {
    File file = DelphiUtils.getResource(FILE_NAME);
    DelphiAST ast = new DelphiAST(file);
    CodeAnalysisResults results = analyser.analyze(ast);
    testFunctions(results);
    testClasses(results);
    testFile();
  }

  public void testFunctions(CodeAnalysisResults results) {
    String[] names = {"tdemo.bshowtrackerclick", "tdemo.getfunction", "tmyclass.myprocedure",
      "tmyclass.setsomething",
      "standaloneprocedure", "standalonefunction"};
    int[] complexities = {1, 0, 2, 0, 3, 1};
    int[] statements = {1, 2, 2, 0, 7, 0};
    int[] calledFunc = {0, 0, 1, 0, 0, 1};
    int[] numArgs = {0, 0, 0, 0, 4, 1};
    boolean[] global = {false, false, false, false, true, true};
    List<FunctionInterface> functions = results.getFunctions();
    assertEquals(6, functions.size()); // checking total function number

    int declarations = 0;
    int accessors = 0;
    int index = 0;
    for (FunctionInterface func : functions) {
      if (func.isDeclaration()) {
        ++declarations;
      }
      if (func.isAccessor()) {
        ++accessors;
      }
      assertEquals(names[index], func.getName()); // checking names
      assertEquals(complexities[index], func.getComplexity()); // checking
                                                               // complexities
      assertEquals(statements[index], func.getStatements().size()); // checking
                                                                    // statements
      assertEquals("Called functions at " + names[index], calledFunc[index], func.getCalledFunctions().length); // chcecking
                                                                                                                // called
                                                                                                                // functions
      assertEquals(names[index], global[index], func.isGlobal()); // checking
                                                                  // if
                                                                  // function
                                                                  // is
                                                                  // global
      assertEquals(names[index], numArgs[index], func.getArguments().length); // number
                                                                              // of
                                                                              // arguments

      if (func.isGlobal()) {
        fileComplexity += func.getComplexity();
      }
      ++index;
    }

    assertEquals("DECLARATIONS COUNT", 4, declarations); // how many
                                                         // declarations
    assertEquals("ACCESSORS COUNT", 2, accessors); // how many accessors
  }

  public void testClasses(CodeAnalysisResults results) {
    String names[] = {"tdemo", "tmyclass", "tmyancestor", "tmyelder"};
    String fnames[] = {"bshowtracker", "field1", "field2", "bshowtracker", "protectedfield"};
    String ftypes[] = {"tbutton", "integer", "integer", "tbutton", "real"};
    int[] functionsCount = {2, 2, 0, 0};
    int[] declarationsCount = {2, 2, 0, 0};
    int[] publicApiCount = {4, 2, 1, 1};
    int[] accessorsCount = {1, 1, 0, 0};
    int[] complexities = {1, 2, 0, 0};
    int[] fieldsCount = {4, 1, 0, 0};
    int[] parentsCount = {3, 0, 1, 0};
    int[] descendants = {0, 1, 1, 2};
    int[] children = {0, 1, 1, 1};

    List<ClassInterface> classes = results.getClasses();
    assertEquals(4, classes.size());

    int index = 0, findex = 0;
    for (ClassInterface cl : classes) {
      assertEquals("NAME", names[index], cl.getName());
      assertEquals(cl.getName(), publicApiCount[index], cl.getPublicApiCount());
      assertEquals(cl.getName(), accessorsCount[index], cl.getAccessorCount());
      assertEquals(cl.getName(), complexities[index], cl.getComplexity());
      assertEquals(cl.getName(), functionsCount[index], cl.getFunctions().length);
      assertEquals(cl.getName(), declarationsCount[index], cl.getFunctions().length);
      assertEquals(cl.getName(), fieldsCount[index], cl.getFields().length);
      assertEquals(cl.getName(), parentsCount[index], cl.getParents().length);
      assertEquals(cl.getName(), descendants[index], cl.getDescendants().length);
      assertEquals(cl.getName(), children[index], cl.getChildren().length);
      for (ClassFieldInterface field : cl.getFields()) { // checking class
                                                         // fields
        assertEquals(cl.getName(), fnames[findex], field.getName());
        assertEquals(cl.getName(), ftypes[findex], field.getType());
        assertEquals(cl.getName(), names[index], field.getParent().getName());
        ++findex;
      }

      fileComplexity += cl.getComplexity();
      ++index;
    }
  }

  public void testFile() {
    assertEquals(7, fileComplexity);
    assertEquals(3.5, fileComplexity / 2.0, 0.0);
  }

}
