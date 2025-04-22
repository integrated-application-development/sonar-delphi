/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
package au.com.integradev.delphi.msbuild.expression;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

class MSBuildWellKnownPropertyHelperTest {

  private static class WellKnownPropertyArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("MSBuildProjectFullPath", "C:\\Source\\Repos\\ConsoleApp1.dproj"),
          Arguments.of("MSBuildProjectFile", "ConsoleApp1.dproj"),
          Arguments.of("MSBuildProjectName", "ConsoleApp1"),
          Arguments.of("MSBuildProjectExtension", ".dproj"),
          Arguments.of("MSBuildProjectDirectory", "C:\\Source\\Repos\\"),
          Arguments.of("MSBuildProjectDirectoryNoRoot", "Source\\Repos\\"),
          Arguments.of("MSBuildThisFileFullPath", "C:\\Source\\Repos\\Props.optset"),
          Arguments.of("MSBuildThisFile", "Props.optset"),
          Arguments.of("MSBuildThisFileName", "Props"),
          Arguments.of("MSBuildThisFileExtension", ".optset"),
          Arguments.of("MSBuildThisFileDirectory", "C:\\Source\\Repos\\"),
          Arguments.of("MSBuildThisFileDirectoryNoRoot", "Source\\Repos\\"),
          Arguments.of("OS", "Windows_NT"));
    }
  }

  @ParameterizedTest(name = "{0}")
  @ArgumentsSource(WellKnownPropertyArgumentsProvider.class)
  void testWellKnownProperties(String propertyName, String expected) {
    var helper =
        new MSBuildWellKnownPropertyHelper(
            "C:\\Source\\Repos\\Props.optset", "C:\\Source\\Repos\\ConsoleApp1.dproj");
    assertThat(helper.getProperty(propertyName)).isEqualTo(expected);
  }

  @Test
  void testCaseInsensitivity() {
    var helper = new MSBuildWellKnownPropertyHelper("FOO", "bar");
    assertThat(helper.getProperty("msbuildthisfile"))
        .isEqualTo(helper.getProperty("MSBuildThisFile"));
  }

  @Test
  void testUnknownPropertyReturnsNull() {
    var helper = new MSBuildWellKnownPropertyHelper("foo", "bar");
    assertThat(helper.getProperty("foo")).isNull();
  }
}
