package org.sonar.plugins.delphi.msbuild.condition;

import java.nio.file.Path;
import org.apache.commons.text.StringSubstitutor;

public final class ExpressionEvaluator {
  private final Path evaluationDirectory;
  private final StringSubstitutor substitutor;

  public ExpressionEvaluator(Path evaluationDirectory, StringSubstitutor substitutor) {
    this.evaluationDirectory = evaluationDirectory;
    this.substitutor = substitutor;
  }

  public String expand(String value) {
    return substitutor.replace(value);
  }

  public Path getEvaluationDirectory() {
    return evaluationDirectory;
  }
}
