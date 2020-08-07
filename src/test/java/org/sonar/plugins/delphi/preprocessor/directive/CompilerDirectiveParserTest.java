package org.sonar.plugins.delphi.preprocessor.directive;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.antlr.runtime.CommonToken;
import org.junit.Before;
import org.junit.Test;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.preprocessor.directive.CompilerDirectiveParser.CompilerDirectiveParserError;
import org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionLexer.ExpressionLexerError;
import org.sonar.plugins.delphi.preprocessor.directive.expression.ExpressionParser.ExpressionParserError;

public class CompilerDirectiveParserTest {
  private CompilerDirectiveParser parser;

  @Before
  public void setup() {
    parser = new CompilerDirectiveParser();
  }

  @Test
  public void testCreateIncludeDirective() {
    CompilerDirective directive = parse("{$include file.inc}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.INCLUDE);

    directive = parse("{$I file.inc}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.INCLUDE);
  }

  @Test
  public void testCreateIfDirective() {
    CompilerDirective directive = parse("{$if True}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.IF);

    assertThatThrownBy(() -> parse("{$if 1..2}"))
        .isInstanceOf(CompilerDirectiveParserError.class)
        .hasCauseInstanceOf(ExpressionLexerError.class);
  }

  @Test
  public void testCreateIfDef() {
    CompilerDirective ifdef = parse("{$IFDEF MY_DEFINITION}");
    CompilerDirective ifndef = parse("{$IFNDEF MY_DEFINITION}");

    assertThat(ifdef.getType()).isEqualTo(CompilerDirectiveType.IFDEF);
    assertThat(ifndef.getType()).isEqualTo(CompilerDirectiveType.IFNDEF);
  }

  @Test
  public void testCreateIfOpt() {
    CompilerDirective directive = parse("{$IFOPT O+}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.IFOPT);
  }

  @Test
  public void testCreateUndefineDirective() {
    CompilerDirective directive = parse("{$undef _DEBUG}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.UNDEFINE);
  }

  @Test
  public void testCreateElseDirective() {
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
  public void testCreateIfEndDirective() {
    CompilerDirective directive = parse("{$ifend}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.IFEND);
  }

  @Test
  public void testCreateEndIfDirective() {
    CompilerDirective directive = parse("{$endif}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.ENDIF);
  }

  @Test
  public void testCreateDefinreDirective() {
    CompilerDirective directive = parse("{$define _DEBUG}");
    assertThat(directive.getType()).isEqualTo(CompilerDirectiveType.DEFINE);
  }

  @Test
  public void testCreateUnsupportedDirectives() {
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
