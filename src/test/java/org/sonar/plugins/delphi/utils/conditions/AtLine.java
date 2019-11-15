package org.sonar.plugins.delphi.utils.conditions;

import org.assertj.core.api.Condition;
import org.sonar.api.batch.fs.TextRange;
import org.sonar.api.batch.sensor.issue.Issue;
import org.sonar.plugins.delphi.pmd.FilePosition;

public class AtLine extends Condition<Issue> {
  private final int line;

  private AtLine(int line) {
    super("rule line " + line);
    this.line = line;
  }

  @Override
  public boolean matches(Issue issue) {
    return getLine(issue) == line;
  }

  public static AtLine atLine(int line) {
    return new AtLine(line);
  }

  private static int getLine(Issue item) {
    TextRange textRange = item.primaryLocation().textRange();

    return (textRange == null) ? FilePosition.UNDEFINED_LINE : textRange.start().line();
  }
}
