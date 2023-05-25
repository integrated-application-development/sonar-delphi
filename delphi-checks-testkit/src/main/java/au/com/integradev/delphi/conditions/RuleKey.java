/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package au.com.integradev.delphi.conditions;

import org.assertj.core.api.Condition;
import org.sonar.api.batch.sensor.issue.Issue;

public final class RuleKey extends Condition<Issue> {
  private final String key;

  private RuleKey(String key) {
    super("ruleKey " + key);
    this.key = key;
  }

  @Override
  public boolean matches(Issue issue) {
    return issue.ruleKey().rule().equals(this.key);
  }

  public static RuleKey ruleKey(String key) {
    return new RuleKey(key);
  }
}
