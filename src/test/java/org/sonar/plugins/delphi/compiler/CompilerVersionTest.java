package org.sonar.plugins.delphi.compiler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.sonar.plugins.delphi.compiler.CompilerVersion.FormatException;

class CompilerVersionTest {
  @Test
  void testFromVersionSymbolShouldThrowOnBadFormat() {
    assertThatThrownBy(() -> CompilerVersion.fromVersionSymbol("VER1234"))
        .isInstanceOf(FormatException.class);

    assertThatThrownBy(() -> CompilerVersion.fromVersionSymbol("VER1"))
        .isInstanceOf(FormatException.class);

    assertThatThrownBy(() -> CompilerVersion.fromVersionSymbol("INVALID"))
        .isInstanceOf(FormatException.class);
  }

  @Test
  void testFromVersionNumberShouldThrowOnBadFormat() {
    assertThatThrownBy(() -> CompilerVersion.fromVersionNumber("123"))
        .isInstanceOf(FormatException.class);

    assertThatThrownBy(() -> CompilerVersion.fromVersionNumber("1.23"))
        .isInstanceOf(FormatException.class);

    assertThatThrownBy(() -> CompilerVersion.fromVersionNumber("ABC"))
        .isInstanceOf(FormatException.class);
  }

  @Test
  void testEquals() {
    assertThat(CompilerVersion.fromVersionNumber("15.0"))
        .isEqualTo(CompilerVersion.fromVersionSymbol("VER150"))
        .isNotEqualTo(CompilerVersion.fromVersionSymbol("VER15"))
        .isNotEqualTo(null)
        .isNotEqualTo(new Object());

    assertThat(CompilerVersion.fromVersionNumber("1.5"))
        .isNotEqualTo(CompilerVersion.fromVersionSymbol("VER150"));
  }

  @Test
  void testHashCode() {
    assertThat(CompilerVersion.fromVersionNumber("15.0"))
        .hasSameHashCodeAs(CompilerVersion.fromVersionSymbol("VER150"))
        .doesNotHaveSameHashCodeAs(CompilerVersion.fromVersionSymbol("VER15"));

    assertThat(CompilerVersion.fromVersionNumber("1.5"))
        .doesNotHaveSameHashCodeAs(CompilerVersion.fromVersionSymbol("VER150"));
  }
}
