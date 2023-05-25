package org.sonar.plugins.communitydelphi.api.check;

import org.sonar.plugins.communitydelphi.api.FatalAnalysisError;

public class IllegalRuleParameterError extends FatalAnalysisError {
  public IllegalRuleParameterError(String message, Throwable cause) {
    super(message, cause);
  }
}
