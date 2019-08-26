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
package org.sonar.plugins.delphi.antlr.directives;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.sonar.plugins.delphi.antlr.directives.exceptions.CompilerDirectiveSyntaxException;
import org.sonar.plugins.delphi.antlr.directives.exceptions.UnsupportedCompilerDirectiveException;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public class CompilerDirectiveParserTest {
  private static final int TEST_FILE_DIRECTIVES_COUNT = 19;
  private static final File TEST_FILE =
      DelphiUtils.getResource("/org/sonar/plugins/delphi/directives/FileWithDirectives.pas");

  private CompilerDirectiveParser parser;

  @Rule public ExpectedException exceptionCatcher = ExpectedException.none();

  @Before
  public void setup() {
    parser = new CompilerDirectiveParser();
  }

  @Test
  public void testCreateIncludeDirective()
      throws CompilerDirectiveSyntaxException, UnsupportedCompilerDirectiveException {
    CompilerDirective directive = parseOne("{$include file.inc}");
    assertEquals(CompilerDirectiveType.INCLUDE, directive.getType());
    assertEquals("file.inc", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(18, directive.getLastCharPosition());

    directive = parseOne("{$I file.inc}");
    assertEquals(CompilerDirectiveType.INCLUDE, directive.getType());
    assertEquals("file.inc", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(12, directive.getLastCharPosition());
  }

  @Test
  public void testCreateUnusedDirective()
      throws CompilerDirectiveSyntaxException, UnsupportedCompilerDirectiveException {
    CompilerDirective directive = parseOne("{$i+}");
    assertEquals(CompilerDirectiveType.UNSUPPORTED, directive.getType());
    assertEquals("", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(4, directive.getLastCharPosition());

    directive = parseOne("{$warn}");
    assertEquals(CompilerDirectiveType.UNSUPPORTED, directive.getType());
    assertEquals("", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(6, directive.getLastCharPosition());

    directive = parseOne("{$R}");
    assertEquals(CompilerDirectiveType.UNSUPPORTED, directive.getType());
    assertEquals("", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(3, directive.getLastCharPosition());
  }

  @Test
  public void testCreateIfDirective()
      throws CompilerDirectiveSyntaxException, UnsupportedCompilerDirectiveException {
    CompilerDirective directive = parseOne("{$if file.inc}");
    assertEquals(CompilerDirectiveType.IF, directive.getType());
    assertEquals("file.inc", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(13, directive.getLastCharPosition());
  }

  @Test
  public void testMissingEndBracketShouldThrow() {
    exceptionCatcher.expect(CompilerDirectiveSyntaxException.class);
    parser.parse("{$define");
  }

  @Test
  public void testMissingEndBracketAndItemShouldThrow() {
    exceptionCatcher.expect(CompilerDirectiveSyntaxException.class);
    parser.parse("{$define ");
  }

  @Test
  public void testUnknownNameShouldNotCreateDirective() {
    List<CompilerDirective> directives = parser.parse("{$not_a_real_compiler_directive}");
    assertThat(directives, empty());
  }

  @Test
  public void testCreateUndefineDirective()
      throws CompilerDirectiveSyntaxException, UnsupportedCompilerDirectiveException {
    CompilerDirective directive = parseOne("{$undef _DEBUG}");
    assertEquals(CompilerDirectiveType.UNDEFINE, directive.getType());
    assertEquals("_DEBUG", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(14, directive.getLastCharPosition());
  }

  @Test
  public void testCreateElseDirective()
      throws CompilerDirectiveSyntaxException, UnsupportedCompilerDirectiveException {
    CompilerDirective directive = parseOne("{$else}");
    assertEquals(CompilerDirectiveType.ELSE, directive.getType());
    assertEquals("", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(6, directive.getLastCharPosition());

    directive = parseOne("(*$else*)");
    assertEquals(CompilerDirectiveType.ELSE, directive.getType());
    assertEquals("", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(8, directive.getLastCharPosition());

    directive = parseOne("\t{$else}\t\n");
    assertEquals(CompilerDirectiveType.ELSE, directive.getType());
    assertEquals("", directive.getItem());
    assertEquals(1, directive.getFirstCharPosition());
    assertEquals(7, directive.getLastCharPosition());

    directive = parseOne("{$elseif}");
    assertEquals(CompilerDirectiveType.ELSE, directive.getType());
    assertEquals("", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(8, directive.getLastCharPosition());
  }

  @Test
  public void testCreateIfEndDirective()
      throws CompilerDirectiveSyntaxException, UnsupportedCompilerDirectiveException {
    CompilerDirective directive = parseOne("{$ifend}");
    assertEquals(CompilerDirectiveType.IFEND, directive.getType());
    assertEquals("", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(7, directive.getLastCharPosition());
  }

  @Test
  public void testCreateDefineDirective()
      throws CompilerDirectiveSyntaxException, UnsupportedCompilerDirectiveException {
    CompilerDirective directive = parseOne("{$define _DEBUG}");
    assertEquals(CompilerDirectiveType.DEFINE, directive.getType());
    assertEquals("_DEBUG", directive.getItem());
    assertEquals(0, directive.getFirstCharPosition());
    assertEquals(15, directive.getLastCharPosition());
  }

  @Test
  public void testProduce() throws IOException, CompilerDirectiveSyntaxException {
    String testFileString = DelphiUtils.readFileContent(TEST_FILE, UTF_8.name());

    String[] names = {
      "include",
      "include",
      "define",
      "undefine",
      "if",
      "else",
      "ifend",
      "ifdef",
      "ifndef",
      "ifdef",
      "else",
      "endif",
      "else",
      "if",
      "ifend",
      "if",
      "ifend",
      "endif",
      "endif"
    };

    String[] items = {
      "include1.inc",
      "include2.inc",
      "TEST",
      "TEST",
      "I_FEEL_HAPPY",
      "",
      "",
      "TEST",
      "UseMe",
      "EnableMemoryLeakReporting",
      "",
      "",
      "",
      "VERSION >= 18",
      "",
      "RTLVersion < 18",
      "",
      "",
      ""
    };

    CompilerDirectiveType[] types = {
      CompilerDirectiveType.INCLUDE, CompilerDirectiveType.INCLUDE, CompilerDirectiveType.DEFINE,
      CompilerDirectiveType.UNDEFINE, CompilerDirectiveType.IF, CompilerDirectiveType.ELSE,
      CompilerDirectiveType.IFEND, CompilerDirectiveType.IFDEF, CompilerDirectiveType.IFDEF,
      CompilerDirectiveType.IFDEF, CompilerDirectiveType.ELSE, CompilerDirectiveType.ENDIF,
      CompilerDirectiveType.ELSE, CompilerDirectiveType.IF, CompilerDirectiveType.IFEND,
      CompilerDirectiveType.IF, CompilerDirectiveType.IFEND, CompilerDirectiveType.ENDIF,
      CompilerDirectiveType.ENDIF
    };

    List<CompilerDirective> allDirectives = parser.parse(testFileString);

    assertEquals(TEST_FILE_DIRECTIVES_COUNT, allDirectives.size());
    for (int i = 0; i < TEST_FILE_DIRECTIVES_COUNT; ++i) {
      assertEquals(String.valueOf(i), types[i], allDirectives.get(i).getType());
      assertEquals(names[i], allDirectives.get(i).getName());
      assertEquals(items[i], allDirectives.get(i).getItem());
    }
  }

  private CompilerDirective parseOne(String data) {
    List<CompilerDirective> directives = parser.parse(data);
    assertThat(directives, hasSize(1));

    return directives.get(0);
  }
}
