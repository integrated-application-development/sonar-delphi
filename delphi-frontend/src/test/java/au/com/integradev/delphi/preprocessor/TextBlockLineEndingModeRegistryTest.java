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
package au.com.integradev.delphi.preprocessor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TextBlockLineEndingModeRegistryTest {
  @ParameterizedTest
  @EnumSource(TextBlockLineEndingMode.class)
  void testEmptyRegistryShouldReturnInitialLineEndingMode(TextBlockLineEndingMode mode) {
    var registry = new TextBlockLineEndingModeRegistry(mode);
    assertThat(registry.getLineEndingMode(123)).isEqualTo(mode);
  }

  @Test
  void testGetLineEndingMode() {
    var registry = new TextBlockLineEndingModeRegistry(TextBlockLineEndingMode.CRLF);

    registry.registerLineEndingMode(TextBlockLineEndingMode.CR, 2);
    registry.registerLineEndingMode(TextBlockLineEndingMode.LF, 4);

    assertThat(registry.getLineEndingMode(1)).isEqualTo(TextBlockLineEndingMode.CRLF);
    assertThat(registry.getLineEndingMode(2)).isEqualTo(TextBlockLineEndingMode.CR);
    assertThat(registry.getLineEndingMode(3)).isEqualTo(TextBlockLineEndingMode.CR);
    assertThat(registry.getLineEndingMode(4)).isEqualTo(TextBlockLineEndingMode.LF);
    assertThat(registry.getLineEndingMode(5)).isEqualTo(TextBlockLineEndingMode.LF);
  }
}
