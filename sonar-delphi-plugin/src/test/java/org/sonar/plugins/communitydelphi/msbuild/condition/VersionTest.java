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
package org.sonar.plugins.communitydelphi.msbuild.condition;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.sonar.plugins.communitydelphi.msbuild.utils.VersionUtils;

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
  void testEqualToSameInstance() {
    Version version = VersionUtils.parse("1.0.0.0").orElseThrow();
    assertThat(version).isEqualTo(version);
  }

  @Test
  void testNotEqualToNull() {
    assertThat(VersionUtils.parse("1.0.0.0").orElseThrow()).isNotEqualTo(null);
  }

  @Test
  void testNotEqualToUnrelatedObject() {
    assertThat(VersionUtils.parse("1.0.0.0").orElseThrow()).isNotEqualTo(new Object());
  }
}
