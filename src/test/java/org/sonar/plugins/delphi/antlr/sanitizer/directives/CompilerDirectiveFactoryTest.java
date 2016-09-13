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
package org.sonar.plugins.delphi.antlr.sanitizer.directives;

import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirective;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirectiveFactory;
import org.sonar.plugins.delphi.antlr.directives.CompilerDirectiveType;
import org.sonar.plugins.delphi.antlr.directives.exceptions.CompilerDirectiveFactorySyntaxException;
import org.sonar.plugins.delphi.antlr.directives.exceptions.CompilerDirectiveFactoryUnsupportedDirectiveException;
import org.sonar.plugins.delphi.debug.FileTestsCommon;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CompilerDirectiveFactoryTest extends FileTestsCommon {

  private static final String TEST_FILE = "/org/sonar/plugins/delphi/directives/FileWithDirectives.pas";
  private static final int TEST_FILE_DIRECTIVES_COUNT = 20;
  CompilerDirectiveFactory factory;
  CompilerDirective directive;

  @Before
  public void setup() {
    factory = new CompilerDirectiveFactory();
  }

  @Test
  public void createIncludeDirectiveTest() throws CompilerDirectiveFactorySyntaxException,
    CompilerDirectiveFactoryUnsupportedDirectiveException {
    directive = factory.create("{$include file.inc}", 0, 19);
    assertEquals(CompilerDirectiveType.INCLUDE, directive.getType());
    assertEquals("file.inc", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(18, directive.getLastCharPosition());

    directive = factory.create("{$I file.inc}", 0, 13);
    assertEquals(CompilerDirectiveType.INCLUDE, directive.getType());
    assertEquals("file.inc", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(12, directive.getLastCharPosition());
  }

  @Test
  public void createUnusedDirectiveTest() throws CompilerDirectiveFactorySyntaxException,
    CompilerDirectiveFactoryUnsupportedDirectiveException {
    directive = factory.create("{$i+}", 0, 5);
    assertEquals(CompilerDirectiveType.UNUSED, directive.getType());
    assertEquals("", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(4, directive.getLastCharPosition());

    directive = factory.create("{$warn}", 0, 7);
    assertEquals(CompilerDirectiveType.UNUSED, directive.getType());
    assertEquals("", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(6, directive.getLastCharPosition());

    directive = factory.create("{$R}", 0, 4);
    assertEquals(CompilerDirectiveType.UNUSED, directive.getType());
    assertEquals("", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(3, directive.getLastCharPosition());
  }

  @Test
  public void createIfDirectiveTest() throws CompilerDirectiveFactorySyntaxException,
    CompilerDirectiveFactoryUnsupportedDirectiveException {
    directive = factory.create("{$if file.inc}", 0, 14);
    assertEquals(CompilerDirectiveType.IF, directive.getType());
    assertEquals("file.inc", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(13, directive.getLastCharPosition());
  }

  @Test
  public void createDefineDirectiveTest() throws CompilerDirectiveFactorySyntaxException,
    CompilerDirectiveFactoryUnsupportedDirectiveException {
    directive = factory.create("{$define _DEBUG}", 0, 16);
    assertEquals(CompilerDirectiveType.DEFINE, directive.getType());
    assertEquals("_DEBUG", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(15, directive.getLastCharPosition());
  }

  @Test
  public void createUndefineDirectiveTest() throws CompilerDirectiveFactorySyntaxException,
    CompilerDirectiveFactoryUnsupportedDirectiveException {
    directive = factory.create("{$undef _DEBUG}", 0, 15);
    assertEquals(CompilerDirectiveType.UNDEFINE, directive.getType());
    assertEquals("_DEBUG", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(14, directive.getLastCharPosition());
  }

  @Test
  public void createElseDirectiveTest() throws CompilerDirectiveFactorySyntaxException,
    CompilerDirectiveFactoryUnsupportedDirectiveException {
    directive = factory.create("{$else}", 0, 6);
    assertEquals(CompilerDirectiveType.ELSE, directive.getType());
    assertEquals("", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(6, directive.getLastCharPosition());

    directive = factory.create("\t{$else}\t\n", 0, 12);
    assertEquals(CompilerDirectiveType.ELSE, directive.getType());
    assertEquals("", directive.getItem());
    assertEquals(1, directive.getFirstCharPosition());
    assertEquals(7, directive.getLastCharPosition());

    directive = factory.create("{$elseif}", 0, 9);
    assertEquals(CompilerDirectiveType.ELSE, directive.getType());
    assertEquals("", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(8, directive.getLastCharPosition());
  }

  @Test
  public void createIfEndDirectiveTest() throws CompilerDirectiveFactorySyntaxException,
    CompilerDirectiveFactoryUnsupportedDirectiveException {
    directive = factory.create("{$ifend}", 0, 8);
    assertEquals(CompilerDirectiveType.IFEND, directive.getType());
    assertEquals("", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(7, directive.getLastCharPosition());
  }

  @Test
  public void produceTest() throws IOException, CompilerDirectiveFactorySyntaxException {
    loadFile(TEST_FILE);

    String names[] = {"include", "include", "define", "undefine", "include", "if", "else", "ifend", "ifdef",
      "ifndef", "ifdef", "else",
      "endif", "else", "if", "ifend", "if", "ifend", "endif", "endif"};
    String items[] = {"include1.inc", "include2.inc", "TEST", "TEST", "error.inc", "I_FEEL_HAPPY", "", "", "TEST",
      "UseMe",
      "EnableMemoryLeakReporting", "", "", "", "VERSION >= 18", "", "RTLVersion < 18", "", "", ""};
    CompilerDirectiveType types[] = {CompilerDirectiveType.INCLUDE, CompilerDirectiveType.INCLUDE,
      CompilerDirectiveType.DEFINE,
      CompilerDirectiveType.UNDEFINE, CompilerDirectiveType.INCLUDE, CompilerDirectiveType.IF,
      CompilerDirectiveType.ELSE,
      CompilerDirectiveType.IFEND, CompilerDirectiveType.IFDEF, CompilerDirectiveType.IFDEF,
      CompilerDirectiveType.IFDEF,
      CompilerDirectiveType.ELSE, CompilerDirectiveType.ENDIF, CompilerDirectiveType.ELSE,
      CompilerDirectiveType.IF,
      CompilerDirectiveType.IFEND, CompilerDirectiveType.IF, CompilerDirectiveType.IFEND,
      CompilerDirectiveType.ENDIF,
      CompilerDirectiveType.ENDIF};

    List<CompilerDirective> allDirectives = factory.produce(testFileString.toString());
    assertEquals(TEST_FILE_DIRECTIVES_COUNT, allDirectives.size());
    for (int i = 0; i < TEST_FILE_DIRECTIVES_COUNT; ++i) {
      assertEquals(String.valueOf(i), types[i], allDirectives.get(i).getType());
      assertEquals(names[i], allDirectives.get(i).getName());
      assertEquals(items[i], allDirectives.get(i).getItem());
    }
  }
}
