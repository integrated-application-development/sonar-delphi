package org.sonar.plugins.delphi.symbol.declaration;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.antlr.DelphiLexer;

class MethodKindTest {
  @Test
  void testInvalidTokenTypeShouldThrow() {
    assertThatThrownBy(() -> MethodKind.fromTokenType(DelphiLexer.ABSTRACT))
        .isInstanceOf(AssertionError.class);
  }
}
