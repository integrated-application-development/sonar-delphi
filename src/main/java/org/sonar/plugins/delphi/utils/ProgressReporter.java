/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions and Fabricio Colombo
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
 * Fabricio Colombo (fabricio.colombo.mva@gmail.com)
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
package org.sonar.plugins.delphi.utils;

/**
 * Class used to report some progress
 */
public class ProgressReporter
{
  private double currentProgress = 0;
  private int currentPercent = 0;
  private double reportProgress = 25;
  private int targetProgress = 100;
  private int percentProgress = 25;
  private ProgressReporterLogger logger = null;
  private boolean firstProgress = true;

  /**
   * Default ctor, no logging!
   */
  public ProgressReporter() {
    logger = new ProgressReporterLogger();
  }

  /**
   * Ctor
   * 
   * @param targetProgress Target progress we want to achieve
   * @param parts How many parts of progress we should report, ex. 4 will report every 25%
   * @param logger report will be written to this logger
   */
  public ProgressReporter(int targetProgress, int parts, ProgressReporterLogger logger) {
    this.targetProgress = targetProgress;
    this.reportProgress = targetProgress / (double) parts;
    this.percentProgress = 100 / parts;
    this.logger = logger;
  }

  /**
   * Progress by one
   * 
   * @return number of reports printed
   */
  public int progress() {
    return progress(1);
  }

  /**
   * Progress by amount
   * 
   * @param amount amount we want to progress
   * @return number of reports printed
   */
  public int progress(int amount)
  {
    int reportCount = reportZeroPercent();
    currentProgress += amount;
    while (currentProgress >= reportProgress) {
      currentProgress -= reportProgress;
      currentPercent = Math.min(currentPercent + percentProgress, 100);
      report();
      ++reportCount;
    }
    return reportCount;
  }

  private int reportZeroPercent() {
    if (firstProgress) {
      firstProgress = false;
      report();
      return 1;
    }
    return 0;
  }

  private void report() {
    logger.log(currentPercent + "% done...");
  }

  /**
   * @return target progress
   */
  public int getTargetProgress() {
    return targetProgress;
  }

}
