package org.sonar.plugins.delphi.nunit;

import java.util.Arrays;

public class TestResult {
  public enum Status {
    PASSED("Passed"),
    FAILED("Failed"),
    SKIPPED("Skipped"),
    INCONCLUSIVE("Inconclusive"),
    UNKNOWN("");

    private final String label;

    Status(String label) {
      this.label = label;
    }

    static Status get(String label) {
      return Arrays.stream(Status.values())
          .filter(s -> s.label.equalsIgnoreCase(label))
          .findFirst()
          .orElse(UNKNOWN);
    }
  }

  private Status status;
  private String rawStatus;
  private double durationSeconds;

  public Status getStatus() {
    return this.status;
  }

  public String getRawStatus() {
    return this.rawStatus;
  }

  public TestResult setStatus(String status) {
    this.rawStatus = status;
    this.status = Status.get(status);
    return this;
  }

  public double getDurationSeconds() {
    return this.durationSeconds;
  }

  public TestResult setDurationSeconds(double d) {
    this.durationSeconds = d;
    return this;
  }
}
