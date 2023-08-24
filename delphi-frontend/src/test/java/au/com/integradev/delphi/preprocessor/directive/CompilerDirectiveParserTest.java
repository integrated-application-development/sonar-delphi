/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.preprocessor.directive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.ast.token.DelphiTokenImpl;
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.preprocessor.directive.CompilerDirectiveParserImpl.CompilerDirectiveParserError;
import au.com.integradev.delphi.preprocessor.directive.expression.ExpressionLexer.ExpressionLexerError;
import au.com.integradev.delphi.preprocessor.directive.expression.ExpressionParser.ExpressionParserError;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.directive.CompilerDirective;
import org.sonar.plugins.communitydelphi.api.directive.CompilerDirectiveParser;
import org.sonar.plugins.communitydelphi.api.directive.ConditionalDirective;
import org.sonar.plugins.communitydelphi.api.directive.DefineDirective;
import org.sonar.plugins.communitydelphi.api.directive.IfDefDirective;
import org.sonar.plugins.communitydelphi.api.directive.IfnDefDirective;
import org.sonar.plugins.communitydelphi.api.directive.IncludeDirective;
import org.sonar.plugins.communitydelphi.api.directive.ParameterDirective;
import org.sonar.plugins.communitydelphi.api.directive.ParameterDirective.ParameterKind;
import org.sonar.plugins.communitydelphi.api.directive.SwitchDirective;
import org.sonar.plugins.communitydelphi.api.directive.SwitchDirective.SwitchKind;
import org.sonar.plugins.communitydelphi.api.directive.UndefineDirective;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

class CompilerDirectiveParserTest {
  private CompilerDirectiveParser parser;

  @BeforeEach
  void setup() {
    parser = new CompilerDirectiveParserImpl(Platform.WINDOWS);
  }

  @Test
  void testCreateIncludeDirective() {
    CompilerDirective directive = parse("{$include file.inc}");
    assertThat(directive).isInstanceOf(IncludeDirective.class);

    directive = parse("{$I file.inc}");
    assertThat(directive).isInstanceOf(IncludeDirective.class);
  }

  @Test
  void testCreateIfDirective() {
    CompilerDirective directive = parse("{$if True}");
    assertThat(directive).isInstanceOf(IfDirective.class);

    assertThatThrownBy(() -> parse("{$if 1..2}"))
        .isInstanceOf(CompilerDirectiveParserError.class)
        .hasCauseInstanceOf(ExpressionLexerError.class);
  }

  @Test
  void testCreateIfDef() {
    CompilerDirective directive = parse("{$IFDEF MY_DEFINITION}");

    assertThat(directive).isInstanceOf(IfDefDirective.class);
  }

  @Test
  void testCreateIfnDef() {
    CompilerDirective directive = parse("{$IFNDEF MY_DEFINITION}");

    assertThat(directive).isInstanceOf(IfnDefDirective.class);
  }

  @Test
  void testCreateIfOpt() {
    CompilerDirective directive = parse("{$IFOPT O+}");

    assertThat(directive).isInstanceOf(IfOptDirective.class);
  }

  @Test
  void testCreateUndefineDirective() {
    CompilerDirective directive = parse("{$undef _DEBUG}");

    assertThat(directive).isInstanceOf(UndefineDirective.class);
  }

  @Test
  void testCreateElseDirective() {
    CompilerDirective directive = parse("{$else}");

    assertThat(directive).isInstanceOf(ElseDirective.class);

    directive = parse("(*$else*)");

    assertThat(directive).isInstanceOf(ElseDirective.class);

    directive = parse("{$ELSE}");

    assertThat(directive).isInstanceOf(ElseDirective.class);
  }

  @Test
  void testCreateElseIfDirective() {
    CompilerDirective directive = parse("{$elseif  Defined(MSWINDOWS)  }");

    assertThat(directive).isInstanceOf(ElseIfDirective.class);

    assertThatThrownBy(() -> parse("{$elseif}"))
        .isInstanceOf(CompilerDirectiveParserError.class)
        .hasCauseInstanceOf(ExpressionParserError.class);
  }

  @Test
  void testCreateIfEndDirective() {
    CompilerDirective directive = parse("{$ifend}");

    assertThat(directive).isInstanceOf(IfEndDirective.class);
  }

  @Test
  void testCreateEndIfDirective() {
    CompilerDirective directive = parse("{$endif}");

    assertThat(directive).isInstanceOf(EndIfDirective.class);
  }

  @Test
  void testCreateDefineDirective() {
    CompilerDirective directive = parse("{$define _DEBUG}");

    assertThat(directive).isInstanceOf(DefineDirective.class);
  }

  @Test
  void testZDirectiveSwitchSyntax() {
    CompilerDirective directive = parse("{$Z+}");

    assertThat(directive).isInstanceOf(SwitchDirective.class);
    assertThat(((SwitchDirective) directive).kind()).isEqualTo(SwitchKind.MINENUMSIZE);
  }

  @Test
  void testZDirectiveLongSyntax() {
    CompilerDirective directive = parse("{$MINENUMSIZE 4}");

    assertThat(directive).isInstanceOf(ParameterDirective.class);
    assertThat(((ParameterDirective) directive).kind()).isEqualTo(ParameterKind.MINENUMSIZE);
  }

  @Test
  void testZDirectiveDigitSyntax() {
    CompilerDirective directive = parse("{$Z4}");

    assertThat(directive).isInstanceOf(SwitchDirective.class);
    assertThat(((SwitchDirective) directive).kind()).isEqualTo(SwitchKind.MINENUMSIZE4);
  }

  @Test
  void testZDirectiveSwitchSyntaxInConditional() {
    CompilerDirective directive = parse("{$IFOPT Z+}");

    assertThat(directive).isInstanceOf(ConditionalDirective.class);
  }

  @Test
  void testCreateSwitchDirective() {
    CompilerDirective directive = parse("{$i+}");

    assertThat(directive).isInstanceOf(SwitchDirective.class);
    assertThat(((SwitchDirective) directive).kind()).isEqualTo(SwitchKind.IOCHECKS);
    assertThat(((SwitchDirective) directive).isActive()).isTrue();
  }

  @Test
  void testCreateUnsupportedDirectives() {
    CompilerDirective directive = parse("{$FOO}");
    assertThat(directive).isNull();
  }

  private CompilerDirective parse(String data) {
    Token token = new CommonToken(DelphiLexer.TkCompilerDirective, data);
    DelphiToken delphiToken = new DelphiTokenImpl(token);
    return parser.parse(delphiToken).orElse(null);
  }
}
