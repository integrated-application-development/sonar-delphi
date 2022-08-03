package org.sonar.plugins.delphi.pmd;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class FilePositionTest {
  static class InvalidPrecisePositionArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(FilePosition.UNDEFINED_LINE, 2, 3, 4),
          Arguments.of(1, FilePosition.UNDEFINED_COLUMN, 3, 4),
          Arguments.of(1, 2, FilePosition.UNDEFINED_LINE, 4),
          Arguments.of(1, 2, 3, FilePosition.UNDEFINED_COLUMN));
    }
  }

  static class InvalidLineLevelPositionArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of(FilePosition.UNDEFINED_LINE, 2),
          Arguments.of(1, FilePosition.UNDEFINED_LINE));
    }
  }

  @ParameterizedTest
  @ArgumentsSource(value = InvalidPrecisePositionArgumentsProvider.class)
  void testInvalidPrecisePositionShouldThrow(
      int beginLine, int beginColumn, int endLine, int endColumn) {
    assertThatThrownBy(() -> FilePosition.from(beginLine, beginColumn, endLine, endColumn))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @ParameterizedTest
  @ArgumentsSource(value = InvalidLineLevelPositionArgumentsProvider.class)
  void testInvalidPrecisePositionShouldThrow(int beginLine, int endLine) {
    assertThatThrownBy(() -> FilePosition.atLineLevel(beginLine, endLine))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
