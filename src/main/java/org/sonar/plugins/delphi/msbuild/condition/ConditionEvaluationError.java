package org.sonar.plugins.delphi.msbuild.condition;

public class ConditionEvaluationError extends RuntimeException {
  public ConditionEvaluationError(String condition, Throwable cause) {
    super(String.format("Condition could not be evaluated: \"%s\"", condition), cause);
  }
}
