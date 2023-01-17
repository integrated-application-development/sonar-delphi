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
package au.com.integradev.delphi.msbuild.condition.utils;

import static org.assertj.core.api.Assertions.assertThat;

import au.com.integradev.delphi.msbuild.utils.NumericUtils;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

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
