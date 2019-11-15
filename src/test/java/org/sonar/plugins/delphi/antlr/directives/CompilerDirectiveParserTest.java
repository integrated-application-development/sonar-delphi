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
import static org.assertj.core.api.Assertions.assertThat;

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
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.INCLUDE);
    assertThat(directive.getItem()).isEqualTo("file.inc");
    assertThat(directive.getFirstCharPosition()).isEqualTo(0);
    assertThat(directive.getLastCharPosition()).isEqualTo(18);

    directive = parseOne("{$I file.inc}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.INCLUDE);
    assertThat(directive.getItem()).isEqualTo("file.inc");
    assertThat(directive.getFirstCharPosition()).isEqualTo(0);
    assertThat(directive.getLastCharPosition()).isEqualTo(12);
  }

  @Test
  public void testCreateUnusedDirective()
      throws CompilerDirectiveSyntaxException, UnsupportedCompilerDirectiveException {
    CompilerDirective directive = parseOne("{$i+}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.UNSUPPORTED);
    assertThat(directive.getItem()).isEqualTo("");
    assertThat(directive.getFirstCharPosition()).isEqualTo(0);
    assertThat(directive.getLastCharPosition()).isEqualTo(4);

    directive = parseOne("{$warn}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.UNSUPPORTED);
    assertThat(directive.getItem()).isEqualTo("");
    assertThat(directive.getFirstCharPosition()).isEqualTo(0);
    assertThat(directive.getLastCharPosition()).isEqualTo(6);

    directive = parseOne("{$R}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.UNSUPPORTED);
    assertThat(directive.getItem()).isEqualTo("");
    assertThat(directive.getFirstCharPosition()).isEqualTo(0);
    assertThat(directive.getLastCharPosition()).isEqualTo(3);
  }

  @Test
  public void testCreateIfDirective()
      throws CompilerDirectiveSyntaxException, UnsupportedCompilerDirectiveException {
    CompilerDirective directive = parseOne("{$if file.inc}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.IF);
    assertThat(directive.getItem()).isEqualTo("file.inc");
    assertThat(directive.getFirstCharPosition()).isEqualTo(0);
    assertThat(directive.getLastCharPosition()).isEqualTo(13);
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
    assertThat(directives).isEmpty();
  }

  @Test
  public void testCreateUndefineDirective()
      throws CompilerDirectiveSyntaxException, UnsupportedCompilerDirectiveException {
    CompilerDirective directive = parseOne("{$undef _DEBUG}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.UNDEFINE);
    assertThat(directive.getItem()).isEqualTo("_DEBUG");
    assertThat(directive.getFirstCharPosition()).isEqualTo(0);
    assertThat(directive.getLastCharPosition()).isEqualTo(14);
  }

  @Test
  public void testCreateElseDirective()
      throws CompilerDirectiveSyntaxException, UnsupportedCompilerDirectiveException {
    CompilerDirective directive = parseOne("{$else}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.ELSE);
    assertThat(directive.getItem()).isEqualTo("");
    assertThat(directive.getFirstCharPosition()).isEqualTo(0);
    assertThat(directive.getLastCharPosition()).isEqualTo(6);

    directive = parseOne("(*$else*)");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.ELSE);
    assertThat(directive.getItem()).isEqualTo("");
    assertThat(directive.getFirstCharPosition()).isEqualTo(0);
    assertThat(directive.getLastCharPosition()).isEqualTo(8);

    directive = parseOne("\t{$else}\t\n");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.ELSE);
    assertThat(directive.getItem()).isEqualTo("");
    assertThat(directive.getFirstCharPosition()).isEqualTo(1);
    assertThat(directive.getLastCharPosition()).isEqualTo(7);

    directive = parseOne("{$elseif}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.ELSE);
    assertThat(directive.getItem()).isEqualTo("");
    assertThat(directive.getFirstCharPosition()).isEqualTo(0);
    assertThat(directive.getLastCharPosition()).isEqualTo(8);
  }

  @Test
  public void testCreateIfEndDirective()
      throws CompilerDirectiveSyntaxException, UnsupportedCompilerDirectiveException {
    CompilerDirective directive = parseOne("{$ifend}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.IFEND);
    assertThat(directive.getItem()).isEqualTo("");
    assertThat(directive.getFirstCharPosition()).isEqualTo(0);
    assertThat(directive.getLastCharPosition()).isEqualTo(7);
  }

  @Test
  public void testCreateDefineDirective()
      throws CompilerDirectiveSyntaxException, UnsupportedCompilerDirectiveException {
    CompilerDirective directive = parseOne("{$define _DEBUG}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.DEFINE);
    assertThat(directive.getItem()).isEqualTo("_DEBUG");
    assertThat(directive.getFirstCharPosition()).isEqualTo(0);
    assertThat(directive.getLastCharPosition()).isEqualTo(15);
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

    assertThat(allDirectives.size()).isEqualTo(TEST_FILE_DIRECTIVES_COUNT);
    for (int i = 0; i < TEST_FILE_DIRECTIVES_COUNT; ++i) {
      assertThat(allDirectives.get(i).getType()).as(String.valueOf(i)).isEqualTo(types[i]);
      assertThat(allDirectives.get(i).getName()).isEqualTo(names[i]);
      assertThat(allDirectives.get(i).getItem()).isEqualTo(items[i]);
    }
  }

  private CompilerDirective parseOne(String data) {
    List<CompilerDirective> directives = parser.parse(data);
    assertThat(directives).hasSize(1);

    return directives.get(0);
  }
}
