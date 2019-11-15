package org.sonar.plugins.delphi.utils.conditions;

import org.assertj.core.api.Condition;
import org.sonar.api.batch.sensor.issue.Issue;

public class RuleKey extends Condition<Issue> {
  private final String ruleKey;

  private RuleKey(String ruleKey) {
    super("ruleKey " + ruleKey);
    this.ruleKey = ruleKey;
  }

  @Override
  public boolean matches(Issue issue) {
    return issue.ruleKey().rule().equals(this.ruleKey);
  }

  public static RuleKey ruleKey(String ruleKey) {
    return new RuleKey(ruleKey);
  }
}
