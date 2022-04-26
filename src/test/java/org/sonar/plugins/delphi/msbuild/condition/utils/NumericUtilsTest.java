package org.sonar.plugins.delphi.msbuild.condition.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.plugins.delphi.msbuild.utils.NumericUtils;

class NumericUtilsTest {
  static class ValidNumericArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("1", 1.0),
          Arguments.of("2.3", 2.3),
          Arguments.of("4.56", 4.56),
          Arguments.of("0x4E", 78.0));
    }
  }

  @ParameterizedTest(name = "\"{0}\" should evaluate to: {1}")
  @ArgumentsSource(ValidNumericArgumentsProvider.class)
  void testValidNumerics(String input, Double expected) {
    assertThat(NumericUtils.parse(input)).hasValue(expected);
  }

  @ParameterizedTest(name = "\"{0}\" should be invalid.")
  @ValueSource(strings = {"", "1.2.3", "0x5G", "@"})
  void testInvalidNumerics(String input) {
    assertThat(NumericUtils.parse(input)).isEmpty();
  }
}
