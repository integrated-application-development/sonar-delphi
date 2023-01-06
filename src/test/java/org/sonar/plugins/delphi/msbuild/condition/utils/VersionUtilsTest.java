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
package org.sonar.plugins.delphi.msbuild.condition.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.plugins.delphi.msbuild.condition.Version;
import org.sonar.plugins.delphi.msbuild.utils.VersionUtils;

class VersionUtilsTest {
  static class ValidVersionArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("1.2", new Version(1, 2)),
          Arguments.of("1.2.3", new Version(1, 2, 3)),
          Arguments.of("1.2.3.4", new Version(1, 2, 3, 4)));
    }
  }

  @ParameterizedTest(name = "\"{0}\" should evaluate to: {1}")
  @ArgumentsSource(ValidVersionArgumentsProvider.class)
  void testValidVersions(String input, Version expected) {
    assertThat(VersionUtils.parse(input)).hasValue(expected);
  }

  @ParameterizedTest(name = "\"{0}\" should be invalid.")
  @ValueSource(strings = {"", "1", "foo", "@", "1.2-SNAPSHOT", "1.2.3.4.5"})
  void testInvalidVersions(String input) {
    assertThat(VersionUtils.parse(input)).isEmpty();
  }
}
