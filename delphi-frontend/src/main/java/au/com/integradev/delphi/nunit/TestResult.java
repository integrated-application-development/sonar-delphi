/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.nunit;

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
