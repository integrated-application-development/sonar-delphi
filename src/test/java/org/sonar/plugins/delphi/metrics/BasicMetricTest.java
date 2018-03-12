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
package org.sonar.plugins.delphi.metrics;

import org.junit.Before;
import org.junit.Test;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.sonar.plugins.delphi.metrics.basicmetrics.Metric;
import org.sonar.plugins.delphi.metrics.basicmetrics.DelphiSource;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.Reader;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BasicMetricTest {

  private DelphiSource source;
  private static final String FILE_NAME = "/org/sonar/plugins/delphi/metrics/MetricsTest.pas";

  @Before
  public void setUp() throws Exception {
    File testFile = DelphiUtils.getResource(FILE_NAME);
    Reader reader = new BufferedReader(new FileReader(testFile));
    source = new DelphiSource(reader);
  }

  @Test
  public void executeOnResource() {
    InputFile pasResource = mock(InputFile.class);
    InputFile dprResource = mock(InputFile.class);
    InputFile dpkResource = mock(InputFile.class);
    InputFile cppResource = mock(InputFile.class);
    when(pasResource.filename()).thenReturn("source.pas");
    when(dprResource.filename()).thenReturn("source.dpr");
    when(cppResource.filename()).thenReturn("source.cpp");
    when(dpkResource.filename()).thenReturn("source.dpk");

    assertTrue(new BasicMetrics(null).executeOnResource(pasResource));
    assertTrue(new BasicMetrics(null).executeOnResource(dprResource));
    assertTrue(new BasicMetrics(null).executeOnResource(dpkResource));
    assertFalse(new BasicMetrics(null).executeOnResource(cppResource));
  }

  @Test
  public void testComments() {
    assertThat(source.getMeasure(Metric.COMMENT_LINES), is(14));
    assertThat(source.getMeasure(Metric.COMMENT_BLANK_LINES), is(1));
    assertThat(source.getMeasure(Metric.PUBLIC_DOC_API), is(2));
  }

  @Test
  public void testLines() {
    assertThat(source.getMeasure(Metric.LINES), is(72));
    assertThat(source.getMeasure(Metric.LINES_OF_CODE), is(43));
  }

}
