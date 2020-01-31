/*
 * SonarQube PMD Plugin
 * Copyright (C) 2012-2019 SonarSource SA
 * mailto:info AT sonarsource DOT com
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
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.delphi.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Constructor;
import org.junit.Test;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RulePriority;

public class PmdLevelUtilsTest {

  @Test
  @SuppressWarnings("deprecation")
  public void testShouldGetPriorityFromLevel() {
    assertThat(PmdLevelUtils.fromLevel(1)).isEqualTo(RulePriority.BLOCKER);
    assertThat(PmdLevelUtils.fromLevel(2)).isEqualTo(RulePriority.CRITICAL);
    assertThat(PmdLevelUtils.fromLevel(3)).isEqualTo(RulePriority.MAJOR);
    assertThat(PmdLevelUtils.fromLevel(4)).isEqualTo(RulePriority.MINOR);
    assertThat(PmdLevelUtils.fromLevel(5)).isEqualTo(RulePriority.INFO);
    assertThat(PmdLevelUtils.fromLevel(-1)).isNull();
    assertThat(PmdLevelUtils.fromLevel(null)).isNull();
  }

  @Test
  public void testShouldGetLevelFromSeverity() {
    assertThat(PmdLevelUtils.toLevel(Severity.BLOCKER)).isEqualTo(1);
    assertThat(PmdLevelUtils.toLevel(Severity.CRITICAL)).isEqualTo(2);
    assertThat(PmdLevelUtils.toLevel(Severity.MAJOR)).isEqualTo(3);
    assertThat(PmdLevelUtils.toLevel(Severity.MINOR)).isEqualTo(4);
    assertThat(PmdLevelUtils.toLevel(Severity.INFO)).isEqualTo(5);
  }

  @Test
  public void testHasPrivateConstructor() throws Exception {
    Constructor<?> constructor = PmdLevelUtils.class.getDeclaredConstructor();
    assertThat(constructor.canAccess(null)).isFalse();
    constructor.setAccessible(true);
    constructor.newInstance();
  }
}
