/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.msbuild.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.enviroment.EnvironmentVariableProvider;
import au.com.integradev.delphi.msbuild.MSBuildState;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ConditionEvaluatorTest {
  @TempDir private Path tempDir;

  @Test
  void testEmptyConditionShouldEvaluateToFalse() {
    assertThat(evaluate("")).isFalse();
  }

  @Test
  void testNonBooleanConditionShouldThrow() {
    assertThatThrownBy(() -> evaluate("'foo'")).isInstanceOf(ConditionEvaluationError.class);
  }

  @Test
  void testLiteralTrueShouldEvaluateToTrue() {
    assertThat(evaluate("true")).isTrue();
  }

  @Test
  void testLiteralFalseShouldEvaluateToFalse() {
    assertThat(evaluate("false")).isFalse();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "'$(TrueProp)'=='true'",
        "'$(FooProp)'=='bar'",
        "HasTrailingSlash('foo/bar/')",
        "HasTrailingSlash('foo\\bar\\')",
        "'$(FooProp)' != 'foo'"
      })
  void testTrueCondition(String condition) {
    assertThat(evaluate(condition)).isTrue();
  }

  private MSBuildState state() {
    var environmentVariableProvider = mock(EnvironmentVariableProvider.class);
    when(environmentVariableProvider.getenv()).thenReturn(Collections.emptyMap());
    when(environmentVariableProvider.getenv(anyString())).thenReturn(null);
    var state = new MSBuildState(tempDir, tempDir, environmentVariableProvider);
    state.setProperty("TrueProp", "true");
    state.setProperty("FooProp", "bar");
    return state;
  }

  private boolean evaluate(String condition) {
    var evaluator = new ConditionEvaluator(state());
    return evaluator.evaluate(condition);
  }
}
