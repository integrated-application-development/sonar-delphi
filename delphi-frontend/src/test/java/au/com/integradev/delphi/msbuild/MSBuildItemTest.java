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
package au.com.integradev.delphi.msbuild;

import static org.assertj.core.api.Assertions.*;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;

class MSBuildItemTest {
  private static final String PROJECT_DIR = "C:\\project";

  static class SupportedWellKnownMetadataArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("FullPath", "C:\\foo\\bar\\baz.qux", "C:\\foo\\bar\\baz.qux"),
          Arguments.of("FullPath", "foo", "C:\\project\\foo"),
          Arguments.of("RootDir", "C:\\foo\\bar\\baz.qux", "C:\\"),
          Arguments.of("RootDir", "foo", "C:\\"),
          Arguments.of("Filename", "C:\\foo\\bar\\baz.qux", "baz"),
          Arguments.of("Filename", "foo", "foo"),
          Arguments.of("Extension", "C:\\foo\\bar\\baz.qux", ".qux"),
          Arguments.of("Extension", "foo", ""),
          Arguments.of("RelativeDir", "C:\\foo\\bar\\baz.qux", "C:\\foo\\bar\\"),
          Arguments.of("RelativeDir", "flarp\\foo", "flarp\\"),
          Arguments.of("RelativeDir", "foo", ""),
          Arguments.of("Directory", "C:\\foo\\bar\\baz.qux", "foo\\bar\\"),
          Arguments.of("Directory", "flarp\\foo", "project\\flarp\\"),
          Arguments.of("Directory", "foo", "project\\"),
          Arguments.of("Identity", "C:\\foo\\bar\\baz.qux", "C:\\foo\\bar\\baz.qux"),
          Arguments.of("Identity", "flarp\\foo", "flarp\\foo"),
          Arguments.of("Identity", "foo", "foo"));
    }
  }

  static class CustomMetadataArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("FooMetadata", "foometadata", "foo value"),
          Arguments.of("FooMetadata", "FooMetadata", "foo value"),
          Arguments.of("foo_metadata", "Foo_Metadata", "foo value"));
    }
  }

  @ParameterizedTest(name = "({1}).{0} = {2}")
  @ArgumentsSource(SupportedWellKnownMetadataArgumentsProvider.class)
  void testWellKnownMetadataEvaluation(String metadataName, String itemIdentity, String expected) {
    Assertions.assertThat(
            // Need to convert to Windows separators in case the test is running on Unix
            FilenameUtils.separatorsToWindows(
                new MSBuildItem(itemIdentity, PROJECT_DIR, Collections.emptyMap())
                    .getMetadata(metadataName)))
        .isEqualTo(expected);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "RecursiveDir",
        "ModifiedTime",
        "CreatedTime",
        "AccessedTime",
        "DefiningProjectFullPath",
        "DefiningProjectDirectory",
        "DefiningProjectName",
        "DefiningProjectExtension"
      })
  void testUnsupportedWellKnownMetadataEvaluatesToEmpty(String metadataName) {
    assertThat(
            new MSBuildItem("C:\\foo\\bar\\baz.qux", PROJECT_DIR, Collections.emptyMap())
                .getMetadata(metadataName))
        .isEmpty();
  }

  @ParameterizedTest
  @ValueSource(strings = {"foometa", "barmeta", "", "MSBuildThisFileDirectory"})
  void testNonexistentCustomMetadataEvaluatesToEmpty(String metadataName) {
    assertThat(
            new MSBuildItem("C:\\foo\\bar\\baz.qux", PROJECT_DIR, Collections.emptyMap())
                .getMetadata(metadataName))
        .isEmpty();
  }

  @ParameterizedTest
  @ArgumentsSource(CustomMetadataArgumentsProvider.class)
  void testExistingCustomMetadataEvaluation(String inputName, String outputName, String expected) {
    assertThat(
            new MSBuildItem("C:\\foo\\bar\\baz.qux", PROJECT_DIR, Map.of(inputName, expected))
                .getMetadata(outputName))
        .isEqualTo(expected);
  }
}
