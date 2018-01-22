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

import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.measures.CoreMetrics;
import org.sonar.plugins.delphi.codecoverage.CoverageFileData;
import org.sonar.plugins.delphi.codecoverage.DelphiCodeCoverageParser;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import java.io.File;
import org.w3c.dom.*;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class DelphiCodeCoverageToolParser implements DelphiCodeCoverageParser
{
  private final File reportFile;
  private final DelphiProjectHelper delphiProjectHelper;

  public DelphiCodeCoverageToolParser(File reportFile, DelphiProjectHelper delphiProjectHelper) {
    this.reportFile = reportFile;
    this.delphiProjectHelper = delphiProjectHelper;
  }

  private void saveCoverageData(CoverageFileData data, SensorContext context) {
    if (data == null) {
      return;
    }
    context.<Double>newMeasure().forMetric(CoreMetrics.COVERAGE).on(data.getResource()).withValue(data.getCoverage()).save();
    context.<Double>newMeasure().forMetric(CoreMetrics.LINE_COVERAGE).on(data.getResource()).withValue(data.getCoverage()).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.LINES_TO_COVER).on(data.getResource()).withValue(data.getTotalLines()).save();
    context.<Integer>newMeasure().forMetric(CoreMetrics.UNCOVERED_LINES).on(data.getResource()).withValue(data.getUncoveredLines()).save();

/*
    Measure<?> lineHits = data.getLineHitsBuilder().build().setPersistenceMode(PersistenceMode.DATABASE);
    try {
      context.newMeasure()
      context.saveMeasure(data.getResource(), overallCoverage);
      context.saveMeasure(data.getResource(), lineCoverage);
      context.saveMeasure(data.getResource(), linesToCover);
      context.saveMeasure(data.getResource(), uncoveredLines);
      context.saveMeasure(data.getResource(), lineHits);
      DelphiUtils.LOG.debug("Saving coverage to: " + data.getResource().absolutePath());
    } catch (Exception e) {
      DelphiUtils.LOG.error("Error saving coverage measure.", e);
    }*/
  }

  void parseReportFile(SensorContext sensorContext)
  {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(reportFile);

      // normalize text representation
      doc.getDocumentElement().normalize();

      NodeList dataNodes = doc.getElementsByTagName("data");
      Element all = (Element)(dataNodes.item(0));
      NodeList srcFiles = all.getElementsByTagName("srcfile");

      for (int f = 0; f < srcFiles.getLength(); f++) {
        Element srcFile = (Element)srcFiles.item(f);
        String fileName = srcFile.getAttributes().getNamedItem("name").getTextContent();

        InputFile sourceFile = delphiProjectHelper.findFileInDirectories(fileName);

        int totalLines = 0;
        int coveredLines = 0;
        CoverageFileData data = new CoverageFileData(sourceFile);

        NodeList lines = srcFile.getElementsByTagName("line");
        for (int n = 0; n < lines.getLength(); n++)
        {
          Node line = lines.item(n);
          String lineNumber = line.getAttributes().getNamedItem("number").getTextContent();
          String covered = line.getAttributes().getNamedItem("covered").getTextContent();
          boolean isCovered = Boolean.valueOf(covered);
          data.getLineHitsBuilder().add(lineNumber, isCovered ? 1 : 0);
          coveredLines += isCovered ? 1 : 0;
          ++totalLines;
        }
        data.setTotalLines(totalLines);
        data.setUncoveredLines(totalLines - coveredLines);
        DelphiUtils.LOG.debug("Coverage (" + fileName + "): " + coveredLines + "/" + totalLines + "("
            + data.getCoverage() + "%)");
        saveCoverageData(data, sensorContext);
      }
    } catch (SAXParseException err) {
      DelphiUtils.LOG.info("SAXParseException");
    } catch (SAXException e) {
      DelphiUtils.LOG.info("SAXException");
      Exception x = e.getException ();
      ((x == null) ? e : x).printStackTrace ();
    } catch (Throwable t) {
      DelphiUtils.LOG.info("Throwable");
      t.printStackTrace ();
    }
  }

  @Override
  public void parse(SensorContext context) {
    if (!reportFile.exists()) {
      return;
    }

    parseReportFile(context);
  }
}
