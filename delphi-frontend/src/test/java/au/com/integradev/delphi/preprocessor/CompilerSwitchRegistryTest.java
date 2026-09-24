/*
 * Sonar Delphi Plugin
 * Copyright (C) 2026 Integrated Application Development
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

import java.util.EnumSet;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sonar.plugins.communitydelphi.api.directive.SwitchDirective.SwitchKind;

class CompilerSwitchRegistryTest {
  private CompilerSwitchRegistry registry;

  @BeforeEach
  void setup() {
    registry = new CompilerSwitchRegistry();
  }

  @Test
  void testAddAndQuerySwitch() {
    registry.addSwitch(SwitchKind.RANGECHECKS, 0, 10);
    assertThat(registry.isActiveSwitch(SwitchKind.RANGECHECKS, 5)).isTrue();
    assertThat(registry.isActiveSwitch(SwitchKind.RANGECHECKS, 15)).isFalse();
  }

  @Test
  void testPushAndPopState() {
    registry.pushState(EnumSet.of(SwitchKind.RANGECHECKS, SwitchKind.IOCHECKS));

    assertThat(registry.popState())
        .containsExactlyInAnyOrder(SwitchKind.RANGECHECKS, SwitchKind.IOCHECKS);
  }

  @Test
  void testPushAndPopEmptyState() {
    registry.pushState(Set.of());

    assertThat(registry.popState()).isEmpty();
  }

  @Test
  void testNestedPushAndPop() {
    registry.pushState(EnumSet.of(SwitchKind.RANGECHECKS));
    registry.pushState(EnumSet.of(SwitchKind.IOCHECKS));

    assertThat(registry.popState()).containsExactly(SwitchKind.IOCHECKS);
    assertThat(registry.popState()).containsExactly(SwitchKind.RANGECHECKS);
  }

  @Test
  void testPopWithoutPushReturnsNull() {
    assertThat(registry.popState()).isNull();
  }

  @Test
  void testPushStateIsIndependentCopy() {
    Set<SwitchKind> activeSwitches = EnumSet.of(SwitchKind.RANGECHECKS);

    registry.pushState(activeSwitches);
    activeSwitches.add(SwitchKind.IOCHECKS);

    assertThat(registry.popState()).containsExactly(SwitchKind.RANGECHECKS);
  }
}
