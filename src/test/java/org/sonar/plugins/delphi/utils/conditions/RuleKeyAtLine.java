package org.sonar.plugins.delphi.utils.conditions;

import static org.assertj.core.condition.AllOf.allOf;
import static org.sonar.plugins.delphi.utils.conditions.AtLine.atLine;
import static org.sonar.plugins.delphi.utils.conditions.RuleKey.ruleKey;

import org.assertj.core.api.Condition;
import org.sonar.api.batch.sensor.issue.Issue;

public class RuleKeyAtLine {
  public static Condition<Issue> ruleKeyAtLine(String ruleKey, int line) {
    return allOf(ruleKey(ruleKey), atLine(line));
  }
}
