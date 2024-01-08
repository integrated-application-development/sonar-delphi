/*
 * Sonar Delphi Plugin
 * Copyright (C) 2024 Integrated Application Development
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
package org.sonar.plugins.communitydelphi.api.check;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleScope;

class ScopeMetadataLoaderTest {
  static class ValidMetadataArgumentsProvider implements ArgumentsProvider {
    @Override
    public Stream<Arguments> provideArguments(ExtensionContext context) {
      return Stream.of(
          Arguments.of("AllScope", RuleScope.ALL),
          Arguments.of("DefaultScope", RuleScope.MAIN),
          Arguments.of("MainScope", RuleScope.MAIN),
          Arguments.of("TestScope", RuleScope.TEST),
          Arguments.of("TestsScope", RuleScope.TEST));
    }
  }

  @ParameterizedTest
  @ArgumentsSource(ValidMetadataArgumentsProvider.class)
  void testGetScope(String ruleKey, RuleScope scope) {
    assertThat(getScope(ruleKey)).isEqualTo(scope);
  }

  @ParameterizedTest
  @ValueSource(strings = {"InvalidSyntax", "DoesNotExist"})
  void testInvalidMetadataShouldThrowIllegalArgumentException(String ruleKey) {
    assertThatThrownBy(() -> getScope(ruleKey)).isInstanceOf(IllegalArgumentException.class);
  }

  private static RuleScope getScope(String ruleKey) {
    MetadataResourcePath metadataResourcePath =
        repository -> "org/sonar/plugins/communitydelphi/api/check/metadata/" + repository;
    ClassLoader classLoader = ScopeMetadataLoaderTest.class.getClassLoader();
    ScopeMetadataLoader loader = new ScopeMetadataLoader(metadataResourcePath, classLoader);
    return loader.getScope(RuleKey.of("test", ruleKey));
  }
}
