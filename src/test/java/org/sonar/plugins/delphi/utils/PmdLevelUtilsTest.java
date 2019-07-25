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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertFalse;

import java.lang.reflect.Constructor;
import org.junit.Test;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RulePriority;

public class PmdLevelUtilsTest {

  @Test
  @SuppressWarnings("deprecation")
  public void testShouldGetPriorityFromLevel() {
    assertThat(PmdLevelUtils.fromLevel(1), is(RulePriority.BLOCKER));
    assertThat(PmdLevelUtils.fromLevel(2), is(RulePriority.CRITICAL));
    assertThat(PmdLevelUtils.fromLevel(3), is(RulePriority.MAJOR));
    assertThat(PmdLevelUtils.fromLevel(4), is(RulePriority.MINOR));
    assertThat(PmdLevelUtils.fromLevel(5), is(RulePriority.INFO));
    assertThat(PmdLevelUtils.fromLevel(-1), is(nullValue()));
    assertThat(PmdLevelUtils.fromLevel(null), is(nullValue()));
  }

  @Test
  public void testShouldGetLevelFromSeverity() {
    assertThat(PmdLevelUtils.toLevel(Severity.BLOCKER), is(1));
    assertThat(PmdLevelUtils.toLevel(Severity.CRITICAL), is(2));
    assertThat(PmdLevelUtils.toLevel(Severity.MAJOR), is(3));
    assertThat(PmdLevelUtils.toLevel(Severity.MINOR), is(4));
    assertThat(PmdLevelUtils.toLevel(Severity.INFO), is(5));
  }

  @Test
  public void testHasPrivateConstructor() throws Exception {
    Constructor constructor = PmdLevelUtils.class.getDeclaredConstructor();
    assertFalse(constructor.canAccess(null));
    constructor.setAccessible(true);
    constructor.newInstance();
  }
}
