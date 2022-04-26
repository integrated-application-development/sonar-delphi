package org.sonar.plugins.delphi.msbuild.condition;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sonar.plugins.delphi.msbuild.utils.VersionUtils;

class VersionTest {
  static class EqualVersionArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("1.0.0.0", "1.0.0.0"),
          Arguments.of("1.1.0.0", "1.1.0.0"),
          Arguments.of("1.1.1.0", "1.1.1.0"),
          Arguments.of("1.1.1.1", "1.1.1.1"));
    }
  }

  static class UnequalVersionArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("2.0.0.0", "1.0.0.0"),
          Arguments.of("1.1.0.0", "1.0.0.0"),
          Arguments.of("1.0.1.0", "1.0.0.0"),
          Arguments.of("1.0.0.1", "1.0.0.0"));
    }
  }

  @ParameterizedTest(name = "\"{0}\" should be equal to: {1}")
  @ArgumentsSource(EqualVersionArgumentsProvider.class)
  void testEqualVersions(String left, String right) {
    assertThat(VersionUtils.parse(left)).isEqualTo(VersionUtils.parse(right));
  }

  @ParameterizedTest(name = "\"{0}\" should not be equal to: {1}")
  @ArgumentsSource(UnequalVersionArgumentsProvider.class)
  void testUnequalVersions(String left, String right) {
    assertThat(VersionUtils.parse(left)).isNotEqualTo(VersionUtils.parse(right));
  }

  @Test
  void testNotEqualToNull() {
    assertThat(VersionUtils.parse("1.0.0.0")).isNotEqualTo(null);
  }

  @Test
  void testNotEqualToUnrelatedObject() {
    assertThat(VersionUtils.parse("1.0.0.0")).isNotEqualTo(new Object());
  }
}
