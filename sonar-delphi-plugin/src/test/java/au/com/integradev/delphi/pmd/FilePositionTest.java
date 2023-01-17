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
package au.com.integradev.delphi.pmd;

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
