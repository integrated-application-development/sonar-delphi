package org.sonar.plugins.delphi.nunit;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public final class ResultsAggregator {
  private static final Logger LOG = Loggers.get(ResultsAggregator.class);

  private int failures = 0;
  private int skipped = 0;
  private int tests = 0;
  private double durationSeconds = 0;

  public ResultsAggregator() {}

  public ResultsAggregator add(TestResult result) {
    this.tests++;
    this.durationSeconds += result.getDurationSeconds();
    switch (result.getStatus()) {
      case SKIPPED:
      case INCONCLUSIVE:
        this.skipped++;
        break;
      case FAILED:
        this.failures++;
        break;
      case PASSED:
        break;
      default:
        LOG.warn(
            "Unexpected test result status: '{}'. Treating as Failure.", result.getRawStatus());
        this.failures++;
        break;
    }

    return this;
  }

  public int getFailures() {
    return this.failures;
  }

  public int getSkipped() {
    return this.skipped;
  }

  public int getTestsRun() {
    return this.tests - this.skipped;
  }

  public double getDurationSeconds() {
    return this.durationSeconds;
  }
}
