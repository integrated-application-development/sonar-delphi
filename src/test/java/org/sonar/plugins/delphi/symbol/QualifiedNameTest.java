package org.sonar.plugins.delphi.symbol;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Collections;
import org.junit.jupiter.api.Test;

class QualifiedNameTest {
  @Test
  void testEmptyQualifiedNameShouldThrow() {
    assertThatThrownBy(() -> new QualifiedName(Collections.emptyList()))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(QualifiedName::of).isInstanceOf(IllegalArgumentException.class);
  }
}
