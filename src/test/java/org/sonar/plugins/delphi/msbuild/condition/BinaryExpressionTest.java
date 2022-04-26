package org.sonar.plugins.delphi.msbuild.condition;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import org.apache.commons.text.StringSubstitutor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.sonar.plugins.delphi.msbuild.condition.Token.TokenType;

class BinaryExpressionTest {
  @TempDir private Path tempDir;

  @Test
  void testInvalidOperatorShouldThrow() {
    BinaryExpression expression =
        new BinaryExpression(
            new StringExpression("foo", false),
            TokenType.END_OF_INPUT,
            new StringExpression("bar", false));

    StringSubstitutor substitutor = mock(StringSubstitutor.class);
    when(substitutor.replace(anyString())).thenAnswer(string -> string);

    ExpressionEvaluator evaluator = new ExpressionEvaluator(tempDir, substitutor);

    assertThatThrownBy(() -> expression.boolEvaluate(evaluator))
        .isInstanceOf(InvalidExpressionException.class);
  }
}
