/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package org.sonar.plugins.delphi.preprocessor.directive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.antlr.runtime.CommonToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirectiveParser.CompilerDirectiveParserError;
import org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionLexer.ExpressionLexerError;
import org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionParser.ExpressionParserError;

class CompilerDirectiveParserTest {
  private CompilerDirectiveParser parser;

  @BeforeEach
  void setup() {
    parser = new CompilerDirectiveParser();
  }

  @Test
  void testCreateIncludeDirective() {
    CompilerDirective directive = parse("{$include file.inc}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.INCLUDE);

    directive = parse("{$I file.inc}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.INCLUDE);
  }

  @Test
  void testCreateIfDirective() {
    CompilerDirective directive = parse("{$if True}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.IF);

    assertThatThrownBy(() -> parse("{$if 1..2}"))
        .isInstanceOf(CompilerDirectiveParserError.class)
        .hasCauseInstanceOf(ExpressionLexerError.class);
  }

  @Test
  void testCreateIfDef() {
    CompilerDirective ifdef = parse("{$IFDEF MY_DEFINITION}");
    CompilerDirective ifndef = parse("{$IFNDEF MY_DEFINITION}");

    assertThat(ifdef.getType()).isEqualTo(CompilerDirectiveType.IFDEF);
    assertThat(ifndef.getType()).isEqualTo(CompilerDirectiveType.IFNDEF);
  }

  @Test
  void testCreateIfOpt() {
    CompilerDirective directive = parse("{$IFOPT O+}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.IFOPT);
  }

  @Test
  void testCreateUndefineDirective() {
    CompilerDirective directive = parse("{$undef _DEBUG}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.UNDEFINE);
  }

  @Test
  void testCreateElseDirective() {
    CompilerDirective directive = parse("{$else}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.ELSE);

    directive = parse("(*$else*)");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.ELSE);

    directive = parse("{$ELSE}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.ELSE);

    assertThatThrownBy(() -> parse("{$elseif}"))
        .isInstanceOf(CompilerDirectiveParserError.class)
        .hasCauseInstanceOf(ExpressionParserError.class);

    directive = parse("{$elseif  Defined(MSWINDOWS)  }");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.ELSEIF);
  }

  @Test
  void testCreateIfEndDirective() {
    CompilerDirective directive = parse("{$ifend}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.IFEND);
  }

  @Test
  void testCreateEndIfDirective() {
    CompilerDirective directive = parse("{$endif}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.ENDIF);
  }

  @Test
  void testCreateDefinreDirective() {
    CompilerDirective directive = parse("{$define _DEBUG}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.DEFINE);
  }

  @Test
  void testCreateUnsupportedDirectives() {
    CompilerDirective directive = parse("{$i+}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.UNSUPPORTED);

    directive = parse("{$FOO}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.UNSUPPORTED);

    directive = parse("{$R}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.UNSUPPORTED);
  }

  private CompilerDirective parse(String data) {
    return parser.parse(new CommonToken(DelphiLexer.TkCompilerDirective, data));
  }
}
