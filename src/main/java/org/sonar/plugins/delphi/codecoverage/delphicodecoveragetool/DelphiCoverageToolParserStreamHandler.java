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
package org.sonar.plugins.delphi.codecoverage.delphicodecoveragetool;

import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.SensorContext;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.api.measures.Measure;
import org.sonar.api.measures.PersistenceMode;
import org.sonar.api.utils.StaxParser.XmlStreamHandler;
import org.sonar.plugins.delphi.codecoverage.CoverageFileData;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import javax.xml.stream.XMLStreamException;

public class DelphiCoverageToolParserStreamHandler implements XmlStreamHandler
{
  private final SensorContext context;
  private final DelphiProjectHelper delphiProjectHelper;

  public DelphiCoverageToolParserStreamHandler(SensorContext context, DelphiProjectHelper delphiProjectHelper) {
    this.context = context;
    this.delphiProjectHelper = delphiProjectHelper;
  }

  @Override
  public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
    rootCursor.advance();
    SMInputCursor fileCursor = rootCursor.descendantElementCursor("srcfile");

    while (fileCursor.getNext() != null) {
      CoverageFileData data = collectCoverageData(fileCursor);
      saveCoverageData(data);
    }
  }

  private void saveCoverageData(CoverageFileData data) {
    if (data == null) {
      return;
    }
    Measure<Double> overallCoverage = new Measure<Double>(CoreMetrics.COVERAGE, data.getCoverage());
    Measure<Double> lineCoverage = new Measure<Double>(CoreMetrics.LINE_COVERAGE, data.getCoverage());
    Measure<Double> linesToCover = new Measure<Double>(CoreMetrics.LINES_TO_COVER, data.getTotalLines());
    Measure<Double> uncoveredLines = new Measure<Double>(CoreMetrics.UNCOVERED_LINES, data.getUncoveredLines());
    Measure<?> lineHits = data.getLineHitsBuilder().build().setPersistenceMode(PersistenceMode.DATABASE);
    try {
      context.saveMeasure(data.getResource(), overallCoverage);
      context.saveMeasure(data.getResource(), lineCoverage);
      context.saveMeasure(data.getResource(), linesToCover);
      context.saveMeasure(data.getResource(), uncoveredLines);
      context.saveMeasure(data.getResource(), lineHits);
      DelphiUtils.LOG.debug("Saving coverage to: " + data.getResource().absolutePath());
    } catch (Exception e) {
      DelphiUtils.LOG.error("Error saving coverage measure.", e);
    }
  }

  private CoverageFileData collectCoverageData(SMInputCursor fileCursor) {
    try {
      String fileName = fileCursor.getAttrValue("name");

      InputFile sourceFile = delphiProjectHelper.findFileInDirectories(fileName);

      int totalLines = 0;
      int coveredLines = 0;

      CoverageFileData data = new CoverageFileData(sourceFile);
      SMInputCursor lineCursor = fileCursor.descendantElementCursor("line");
      while (lineCursor.getNext() != null) {
        if (!lineCursor.asEvent().isStartElement()) {
          continue;
        }
        String lineNumber = lineCursor.getAttrValue("number");
        boolean isCovered = Boolean.valueOf(lineCursor.getAttrValue("covered"));
        data.getLineHitsBuilder().add(lineNumber, isCovered ? 1 : 0);
        coveredLines += isCovered ? 1 : 0;
        ++totalLines;
      }

      data.setTotalLines(totalLines);
      data.setUncoveredLines(totalLines - coveredLines);
      DelphiUtils.LOG.debug("Coverage (" + fileName + "): " + coveredLines + "/" + totalLines + "("
        + data.getCoverage() + "%)");
      return data;
    } catch (Exception e) {
      throw new RuntimeException("Failure trying collect coverage data.", e);
    }
  }
}
