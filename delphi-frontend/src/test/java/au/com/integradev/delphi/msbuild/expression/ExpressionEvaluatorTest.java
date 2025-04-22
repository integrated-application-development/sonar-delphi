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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.enviroment.EnvironmentVariableProvider;
import au.com.integradev.delphi.msbuild.MSBuildItem;
import au.com.integradev.delphi.msbuild.MSBuildState;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ExpressionEvaluatorTest {
  private @TempDir Path tempDir;

  private MSBuildState state() {
    var environmentVariableProvider = mock(EnvironmentVariableProvider.class);
    when(environmentVariableProvider.getenv()).thenReturn(Collections.emptyMap());
    when(environmentVariableProvider.getenv(anyString())).thenReturn(null);
    return new MSBuildState(tempDir, tempDir, environmentVariableProvider);
  }

  @Test
  void testSimpleString() {
    assertThat(new ExpressionEvaluator(state()).eval("hello world")).isEqualTo("hello world");
  }

  @Test
  void testProperties() {
    MSBuildState state = state();
    state.setProperty("foo", "I am a foo property!");
    state.setProperty("bar", "I am a bar property!");
    assertThat(new ExpressionEvaluator(state).eval("hello $(foo) $(BAR) $(baz)"))
        .isEqualTo("hello I am a foo property! I am a bar property! ");
  }

  @Test
  void testWhitespacedProperties() {
    MSBuildState state = state();
    state.setProperty("foo", "I am a foo property!");
    assertThat(new ExpressionEvaluator(state).eval("[$(foo)]<$( foo )>"))
        .isEqualTo("[I am a foo property!]<>");
  }

  @Test
  void testItems() {
    MSBuildState state = state();
    state.addItems(
        "MyItem",
        List.of(
            new MSBuildItem("foo/bar/baz", "C:/project", Collections.emptyMap()),
            new MSBuildItem("bar/flarp", "C:/project", Collections.emptyMap()),
            new MSBuildItem("bonk", "C:/project", Collections.emptyMap())));
    assertThat(new ExpressionEvaluator(state).eval("hello @(MyItem) world"))
        .isEqualTo("hello foo/bar/baz;bar/flarp;bonk world");
  }

  @Test
  void testItemsWithCustomSeparators() {
    MSBuildState state = state();
    state.addItems(
        "MyItem",
        List.of(
            new MSBuildItem("foo/bar/baz", "C:/project", Collections.emptyMap()),
            new MSBuildItem("bar/flarp", "C:/project", Collections.emptyMap()),
            new MSBuildItem("bonk", "C:/project", Collections.emptyMap())));
    assertThat(new ExpressionEvaluator(state).eval("hello @(MyItem, ':') world"))
        .isEqualTo("hello foo/bar/baz:bar/flarp:bonk world");
  }

  @Test
  void testItemTransforms() {
    MSBuildState state = state();
    state.addItems(
        "MyItem",
        List.of(
            new MSBuildItem("foo/bar/baz.qux", "C:/project", Collections.emptyMap()),
            new MSBuildItem("bar/flarp.qux", "C:/project", Collections.emptyMap()),
            new MSBuildItem("bonk", "C:/project", Collections.emptyMap())));
    assertThat(new ExpressionEvaluator(state).eval("hello @(MyItem->'%(Filename)') world"))
        .isEqualTo("hello baz;flarp;bonk world");
  }

  @Test
  void testNullString() {
    assertThat(new ExpressionEvaluator(state()).eval(null)).isNull();
  }

  @Test
  void testEmptyString() {
    assertThat(new ExpressionEvaluator(state()).eval("    ")).isEqualTo("    ");
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "$([System.DateTime]::Now)",
        "$(MyValue.ToLower())",
        "@(Foo->Bar())",
        "@(Foo->'%(Bar)'->Baz())",
        "$(^)",
        "$(MyValue:)",
        "@(Foo->)",
        "$(!",
        "@foo",
        "%@$bar)"
      })
  void testUnsupportedOrInvalidExpressions(String expression) {
    var evaluator = new ExpressionEvaluator(state());
    assertThat(evaluator.eval(expression)).isEqualTo(expression);
  }
}
