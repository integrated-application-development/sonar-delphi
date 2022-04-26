package org.sonar.plugins.delphi.msbuild.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sonar.plugins.delphi.enviroment.EnvironmentVariableProvider;
import org.sonar.plugins.delphi.msbuild.ProjectProperties;
import org.sonar.plugins.delphi.msbuild.condition.FunctionCallExpression.ArgumentCountMismatchException;
import org.sonar.plugins.delphi.msbuild.condition.FunctionCallExpression.ScalarFunctionWithMultipleItemsException;
import org.sonar.plugins.delphi.msbuild.condition.FunctionCallExpression.UnknownFunctionException;

class ExpressionEvaluationTest {
  static class BooleanArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("true", true),
          Arguments.of("on", true),
          Arguments.of("yes", true),
          Arguments.of("!false", true),
          Arguments.of("!off", true),
          Arguments.of("!no", true),
          Arguments.of("false", false),
          Arguments.of("off", false),
          Arguments.of("no", false),
          Arguments.of("!true", false),
          Arguments.of("!on", false),
          Arguments.of("!yes", false),
          Arguments.of("Exists('')", false),
          Arguments.of("HasTrailingSlash('')", false),
          Arguments.of("HasTrailingSlash('Foo')", false),
          Arguments.of("HasTrailingSlash('Foo/')", true),
          Arguments.of("HasTrailingSlash('Foo\\')", true),
          Arguments.of("HasTrailingSlash(';;;;Foo\\;;;')", true),
          Arguments.of("HasTrailingSlash(';;;;;;;;;;;;')", false),
          Arguments.of("true == true", true),
          Arguments.of("true != true", false),
          Arguments.of("false == false", true),
          Arguments.of("false != false", false),
          Arguments.of("true == false", false),
          Arguments.of("true != false", true),
          Arguments.of("true or true", true),
          Arguments.of("true or false", true),
          Arguments.of("false or true", true),
          Arguments.of("false or false", false),
          Arguments.of("true and true", true),
          Arguments.of("true and false", false),
          Arguments.of("false and false", false),
          Arguments.of("'foo' == 'foo'", true),
          Arguments.of("'foo' != 'foo'", false),
          Arguments.of("'foo' == 'bar'", false),
          Arguments.of("'foo' != 'bar'", true),
          Arguments.of("1 == 2", false),
          Arguments.of("1 == 1", true),
          Arguments.of("1 != 2", true),
          Arguments.of("1 != 1", false),
          Arguments.of("1 > 2", false),
          Arguments.of("2 > 1", true),
          Arguments.of("1 >= 2", false),
          Arguments.of("2 >= 1", true),
          Arguments.of("2 >= 2", true),
          Arguments.of("1 < 2", true),
          Arguments.of("2 < 1", false),
          Arguments.of("1 <= 2", true),
          Arguments.of("2 <= 1", false),
          Arguments.of("2 <= 2", true),
          Arguments.of("1 == 2.0.0.0", false),
          Arguments.of("1 == 1.0.0.0", false),
          Arguments.of("1 != 2.0.0.0", true),
          Arguments.of("1 != 1.0.0.0", true),
          Arguments.of("1 > 2.0.0.0", false),
          Arguments.of("2 > 1.0.0.0", true),
          Arguments.of("1 >= 2.0.0.0", false),
          Arguments.of("2 >= 1.0.0.0", true),
          Arguments.of("2 >= 2.0.0.0", false),
          Arguments.of("1 < 2.0.0.0", true),
          Arguments.of("2 < 1.0.0.0", false),
          Arguments.of("1 <= 2.0.0.0", true),
          Arguments.of("2 <= 1.0.0.0", false),
          Arguments.of("2 <= 2.0.0.0", true),
          Arguments.of("1.0.0.0 == 2", false),
          Arguments.of("1.0.0.0 == 1", false),
          Arguments.of("1.0.0.0 != 2", true),
          Arguments.of("1.0.0.0 != 1", true),
          Arguments.of("1.0.0.0 > 2", false),
          Arguments.of("2.0.0.0 > 1", true),
          Arguments.of("1.0.0.0 >= 2", false),
          Arguments.of("2.0.0.0 >= 1", true),
          Arguments.of("2.0.0.0 >= 2", true),
          Arguments.of("1.0.0.0 < 2", true),
          Arguments.of("2.0.0.0 < 1", false),
          Arguments.of("1.0.0.0 <= 2", true),
          Arguments.of("2.0.0.0 <= 1", false),
          Arguments.of("2.0.0.0 <= 2", false),
          Arguments.of("1.0.0.0 == 2.0.0.0", false),
          Arguments.of("1.0.0.0 == 1.0.0.0", true),
          Arguments.of("1.0.0.0 != 2.0.0.0", true),
          Arguments.of("1.0.0.0 != 1.0.0.0", false),
          Arguments.of("1.0.0.0 > 2.0.0.0", false),
          Arguments.of("2.0.0.0 > 1.0.0.0", true),
          Arguments.of("1.0.0.0 >= 2.0.0.0", false),
          Arguments.of("2.0.0.0 >= 1.0.0.0", true),
          Arguments.of("2.0.0.0 >= 2.0.0.0", true),
          Arguments.of("1.0.0.0 < 2.0.0.0", true),
          Arguments.of("2.0.0.0 < 1.0.0.0", false),
          Arguments.of("1.0.0.0 <= 2.0.0.0", true),
          Arguments.of("2.0.0.0 <= 1.0.0.0", false),
          Arguments.of("2.0.0.0 <= 2.0.0.0", true),
          Arguments.of("1.1.0.0 > 1.0.0.0", true),
          Arguments.of("1.0.1.0 > 1.0.0.0", true),
          Arguments.of("1.0.0.1 > 1.0.0.0", true),
          Arguments.of("0", null),
          Arguments.of("12.34", null),
          Arguments.of("foo", null),
          Arguments.of("'foo'", null));
    }
  }

  static class NumericArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("0", 0.0),
          Arguments.of("12.34", 12.34),
          Arguments.of("+12.34", 12.34),
          Arguments.of("-12.34", -12.34),
          Arguments.of("12.34.56", null),
          Arguments.of("12.34.56.78", null),
          Arguments.of("foo", null),
          Arguments.of("'foo'", null),
          Arguments.of("true", null),
          Arguments.of("!$(FOO)", null));
    }
  }

  static class VersionArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("1", null),
          Arguments.of("1.2", new Version(1, 2)),
          Arguments.of("1.2.3", new Version(1, 2, 3)),
          Arguments.of("1.2.3.4", new Version(1, 2, 3, 4)),
          Arguments.of("foo", null),
          Arguments.of("'foo'", null),
          Arguments.of("true", null),
          Arguments.of("Exists('foo')", null));
    }
  }

  static class UnexpandedStringProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("foo", "foo"),
          Arguments.of("'foo'", "foo"),
          Arguments.of("$(foo)", "$(foo)"),
          Arguments.of("'$(foo)'", "$(foo)"),
          Arguments.of("$(bar)", "$(bar)"),
          Arguments.of("true", "true"),
          Arguments.of("123", "123"),
          Arguments.of("!true", "!true"),
          Arguments.of("Exists('foo')", null));
    }
  }

  static class InvalidExpressionProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("!'foo'", InvalidExpressionException.class),
          Arguments.of("'foo' and 'bar'", InvalidExpressionException.class),
          Arguments.of("true and 'foo'", InvalidExpressionException.class),
          Arguments.of("'foo' or true", InvalidExpressionException.class),
          Arguments.of("true > false", InvalidExpressionException.class),
          Arguments.of("1 > false", InvalidExpressionException.class),
          Arguments.of("1.0.0.0 > false", InvalidExpressionException.class),
          Arguments.of("true > 1", InvalidExpressionException.class),
          Arguments.of("true > 1.0.0.0", InvalidExpressionException.class),
          Arguments.of("'foo' == Exists('bar')", InvalidExpressionException.class),
          Arguments.of("HasTrailingSlash('foo') == 'bar'", InvalidExpressionException.class),
          Arguments.of("Foo('bar')", UnknownFunctionException.class),
          Arguments.of("Exists('foo', 'bar')", ArgumentCountMismatchException.class),
          Arguments.of("HasTrailingSlash('foo', 'bar')", ArgumentCountMismatchException.class),
          Arguments.of(
              "HasTrailingSlash('foo;bar')", ScalarFunctionWithMultipleItemsException.class));
    }
  }

  static class ExpandedStringProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("foo", "foo"),
          Arguments.of("'foo'", "foo"),
          Arguments.of("$(foo)", "bar"),
          Arguments.of("'$(foo)'", "bar"),
          Arguments.of("$(bar)", ""),
          Arguments.of("true", "true"),
          Arguments.of("123", "123"),
          Arguments.of("!$(foo)", "!bar"));
    }
  }

  @ParameterizedTest(name = "\"{0}\" should evaluate to boolean value: {1}")
  @ArgumentsSource(BooleanArgumentsProvider.class)
  void testBooleanExpressions(String input, Boolean expected) {
    assertThat(parse(input).boolEvaluate(expressionEvaluator()).orElse(null)).isEqualTo(expected);
  }

  @ParameterizedTest(name = "\"{0}\" should evaluate to numeric value: {1}")
  @ArgumentsSource(NumericArgumentsProvider.class)
  void testNumericExpressions(String input, Double expected) {
    assertThat(parse(input).numericEvaluate(expressionEvaluator()).orElse(null))
        .isEqualTo(expected);
  }

  @ParameterizedTest(name = "\"{0}\" should evaluate to version value: {1}")
  @ArgumentsSource(VersionArgumentsProvider.class)
  void testVersionExpressions(String input, Version expected) {
    assertThat(parse(input).versionEvaluate(expressionEvaluator()).orElse(null))
        .isEqualTo(expected);
  }

  @ParameterizedTest(name = "\"{0}\" should evaluate to version value: {1}")
  @ArgumentsSource(UnexpandedStringProvider.class)
  void testUnexpandedStringExpressions(String input, String expected) {
    assertThat(parse(input).getValue().orElse(null)).isEqualTo(expected);
  }

  @ParameterizedTest(name = "\"{0}\" should evaluate to version value: {1}")
  @ArgumentsSource(ExpandedStringProvider.class)
  void testExpandedStringExpressions(String input, String expected) {
    assertThat(parse(input).getExpandedValue(expressionEvaluator()).orElse(null))
        .isEqualTo(expected);
  }

  @ParameterizedTest(name = "\"{0}\" should have thrown: {1}")
  @ArgumentsSource(InvalidExpressionProvider.class)
  void testInvalidExpressions(String input, Class<Exception> expectedException) {
    assertThatThrownBy(() -> parse(input).boolEvaluate(expressionEvaluator()))
        .isExactlyInstanceOf(expectedException);
  }

  private static Expression parse(String data) {
    ConditionLexer lexer = new ConditionLexer();
    List<Token> tokens = lexer.lex(data);

    ConditionParser parser = new ConditionParser();
    return parser.parse(tokens);
  }

  private static ProjectProperties properties() {
    var environmentVariableProvider = mock(EnvironmentVariableProvider.class);
    when(environmentVariableProvider.getenv()).thenReturn(Map.of("foo", "bar"));
    when(environmentVariableProvider.getenv("foo")).thenReturn("bar");
    return ProjectProperties.create(environmentVariableProvider, null);
  }

  private static ExpressionEvaluator expressionEvaluator() {
    return new ExpressionEvaluator(
        FileUtils.getTempDirectory().toPath(), properties().substitutor());
  }
}
