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
package org.sonar.plugins.delphi.utils;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ProgressReporterTest 
{ 
  @Test
  public void testDefaultReporter() {
    ProgressReporter reporter = new ProgressReporter();
    testReporter(reporter, 4);
  }
  
  @Test
  public void testCustomReporter() {
    ProgressReporter reporter = new ProgressReporter(50, 2, new ProgressReporterLogger() );
    testReporter(reporter, 2);
  }
  
  public void testReporter(ProgressReporter reporter, int expectedReports)
  {
    int numReports = 0;
    for(int i = 0; i < reporter.getTargetProgress(); i++) {
      if(reporter.progress()) {
        ++numReports;
      }
    }
    assertEquals(expectedReports, numReports);
  }
}
