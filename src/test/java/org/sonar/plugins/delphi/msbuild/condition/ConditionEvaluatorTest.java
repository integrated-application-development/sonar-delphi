package org.sonar.plugins.delphi.msbuild.condition;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.plugins.delphi.enviroment.EnvironmentVariableProvider;
import org.sonar.plugins.delphi.msbuild.ProjectProperties;

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

  private static ProjectProperties properties() {
    var environmentVariableProvider = mock(EnvironmentVariableProvider.class);
    when(environmentVariableProvider.getenv()).thenReturn(Collections.emptyMap());
    when(environmentVariableProvider.getenv(anyString())).thenReturn(null);
    return ProjectProperties.create(environmentVariableProvider, null);
  }

  private boolean evaluate(String condition) {
    var evaluator = new ConditionEvaluator(properties(), tempDir);
    return evaluator.evaluate(condition);
  }
}
