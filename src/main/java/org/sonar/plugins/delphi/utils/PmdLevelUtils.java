package org.sonar.plugins.delphi.utils;

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

import java.util.Objects;
import javax.annotation.Nullable;

import org.sonar.api.profiles.RulesProfile;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.Rule;
import org.sonar.api.rules.RulePriority;

/**
 * A helper class which converts between PMD levels and Sonar {@link Severity}/{@link RulePriority}
 *
 * RulePriority is deprecated and should be removed as soon as Severity is a viable option
 * The Sonar API still returns or expects RulePriority in several places, including
 * {@link RulesProfile#activateRule} and {@link Rule#setSeverity}
 */
@SuppressWarnings("deprecation")
public final class PmdLevelUtils {

  private static final int INDEX_LEVEL = RulePriority.values().length;
  private PmdLevelUtils() {
    // only static methods
  }

  public static RulePriority fromLevel(@Nullable Integer level) {

    if (Objects.isNull(level)) {
      return null;
    }

    final int index = Math.abs(INDEX_LEVEL - level);

    return (index < INDEX_LEVEL) ? RulePriority.valueOfInt(index) : null;
  }

  public static Integer toLevel(String severity) {
    return Math.abs(Severity.ALL.indexOf(severity) - INDEX_LEVEL);
  }
}
