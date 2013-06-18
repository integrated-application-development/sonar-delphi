/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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
package org.sonar.plugins.delphi.codecoverage;

import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.PropertiesBuilder;
import org.sonar.plugins.delphi.core.DelphiFile;

/**
 * Class that holds coverage data for each source file, used in AQTime purifier
 */
public class CoverageFileData {

  private double coverage = 0.0;
  private double totalLines = 0.0;
  private double uncoveredLines = 0.0;
  private DelphiFile resource;
  private PropertiesBuilder<String, Integer> lineHitsBuilder;

  /**
   * ctor
   * 
   * @param res
   *          Delphi file
   */
  public CoverageFileData(DelphiFile res) {
    resource = res;
    lineHitsBuilder = new PropertiesBuilder<String, Integer>(CoreMetrics.COVERAGE_LINE_HITS_DATA);
  }

  /**
   * @return the coverage
   */
  public double getCoverage() {
    return coverage;
  }

  /**
   * @return the totalLines
   */
  public double getTotalLines() {
    return totalLines;
  }

  /**
   * @return the uncoveredLines
   */
  public double getUncoveredLines() {
    return uncoveredLines;
  }

  /**
   * @return the resource
   */
  public DelphiFile getResource() {
    return resource;
  }

  /**
   * @return the lineHitsBuilder
   */
  public PropertiesBuilder<String, Integer> getLineHitsBuilder() {
    return lineHitsBuilder;
  }

  /**
   * @param coverage
   *          the coverage to set
   */
  public void setCoverage(double coverage) {
    this.coverage = coverage;
  }

  /**
   * @param totalLines
   *          the totalLines to set
   */
  public void setTotalLines(double totalLines) {
    this.totalLines = totalLines;
  }

  /**
   * @param uncoveredLines
   *          the uncoveredLines to set
   */
  public void setUncoveredLines(double uncoveredLines) {
    this.uncoveredLines = uncoveredLines;
  }
}
