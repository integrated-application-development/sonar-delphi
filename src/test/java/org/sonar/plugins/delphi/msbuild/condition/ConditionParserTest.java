package org.sonar.plugins.delphi.msbuild.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.msbuild.condition.ConditionParser.ConditionParserError;
import org.sonar.plugins.delphi.msbuild.condition.Token.TokenType;

class ConditionParserTest {
  @Test
  void testBinary() {
    assertThat(parse("true and false")).isInstanceOf(BinaryExpression.class);
    assertThat(parse("true or false")).isInstanceOf(BinaryExpression.class);
    assertThat(parse("'foo' == 'bar'")).isInstanceOf(BinaryExpression.class);
    assertThat(parse("'foo' != 'bar'")).isInstanceOf(BinaryExpression.class);
  }

  @Test
  void testNot() {
    assertThat(parse("!'foo'")).isInstanceOf(NotExpression.class);
    assertThat(parse("!(true)")).isInstanceOf(NotExpression.class);
    assertThatThrownBy(() -> parse("!")).isInstanceOf(ConditionParserError.class);
  }

  @Test
  void testString() {
    assertThat(parse("'Quoted string'")).isInstanceOf(StringExpression.class);
    assertThat(parse("foo")).isInstanceOf(StringExpression.class);
    assertThat(parse("$(FOO)")).isInstanceOf(StringExpression.class);
  }

  @Test
  void testNumeric() {
    assertThat(parse("123")).isInstanceOf(NumericExpression.class);
    assertThat(parse("123.45")).isInstanceOf(NumericExpression.class);
    assertThat(parse("123.45.67")).isInstanceOf(NumericExpression.class);
    assertThat(parse("123.45.67.89")).isInstanceOf(NumericExpression.class);
    assertThat(parse("0x3c")).isInstanceOf(NumericExpression.class);
  }

  @Test
  void testFunction() {
    assertThat(parse("Exists('./foo/bar')")).isInstanceOf(FunctionCallExpression.class);
    assertThat(parse("Foo()")).isInstanceOf(FunctionCallExpression.class);
    assertThatThrownBy(() -> parse("HasTrailingSlash('./foo/bar'"))
        .isInstanceOf(ConditionParserError.class);
    assertThatThrownBy(
            () -> {
              ConditionParser parser = new ConditionParser();
              parser.parse(List.of(new Token(TokenType.FUNCTION, "HasTrailingSlash")));
            })
        .isInstanceOf(ConditionParserError.class);
  }

  @Test
  void testParenthesizedExpression() {
    assertThat(parse("(true and false)")).isInstanceOf(BinaryExpression.class);
    assertThatThrownBy(() -> parse("(true and false")).isInstanceOf(ConditionParserError.class);
  }

  @Test
  void testNoExpressionShouldThrowException() {
    assertThatThrownBy(() -> parse("and")).isInstanceOf(ConditionParserError.class);
  }

  @Test
  void testUnexpectedTrailingTokenShouldThrowException() {
    assertThatThrownBy(() -> parse("foo bar")).isInstanceOf(ConditionParserError.class);
  }

  private static Expression parse(String data) {
    ConditionLexer lexer = new ConditionLexer();
    List<Token> tokens = lexer.lex(data);

    ConditionParser parser = new ConditionParser();
    return parser.parse(tokens);
  }
}
