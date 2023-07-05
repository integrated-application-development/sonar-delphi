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
package au.com.integradev.delphi.nunit;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public final class ResultsAggregator {
  private static final Logger LOG = Loggers.get(ResultsAggregator.class);

  private int failures;
  private int skipped;
  private int tests;
  private double durationSeconds;

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
