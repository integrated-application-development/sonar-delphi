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
package org.sonar.plugins.delphi.coverage.delphicodecoveragetool;

import java.io.File;
import java.util.Arrays;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.coverage.DelphiCoverageParser;
import org.sonar.plugins.delphi.msbuild.DelphiProjectHelper;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DelphiCodeCoverageToolParser implements DelphiCoverageParser {
  public static final String KEY = "dcc";
  private static final Logger LOG = Loggers.get(DelphiCodeCoverageToolParser.class);
  private final DelphiProjectHelper delphiProjectHelper;

  public DelphiCodeCoverageToolParser(DelphiProjectHelper delphiProjectHelper) {
    this.delphiProjectHelper = delphiProjectHelper;
  }

  private void parseLineHit(String lineHit, int numLines, NewCoverage newCoverage) {
    int eq = lineHit.indexOf('=');
    if (eq > 0) {
      int lineNumber = Integer.parseInt(lineHit.substring(0, eq));
      int lineHits = Integer.parseInt(lineHit.substring(eq + 1));
      if (lineNumber > numLines) {
        LOG.debug(
            "skipping line hit on line {} because it's beyond the end of the file", lineNumber);
      } else {
        newCoverage.lineHits(lineNumber, lineHits);
      }
    }
  }

  private void parseValue(String lineCoverage, NewCoverage newCoverage, int numLines) {
    Arrays.stream(lineCoverage.split(";")).forEach(s -> parseLineHit(s, numLines, newCoverage));
  }

  private void parseFileNode(SensorContext sensorContext, Node srcFile) {
    String fileName = srcFile.getAttributes().getNamedItem("name").getTextContent();
    InputFile sourceFile = delphiProjectHelper.getFileFromBasename(fileName);

    if (sourceFile == null) {
      LOG.debug("File not found in project: {}", fileName);
      return;
    }
    LOG.debug("Parsing line hit information for file: {}", fileName);

    NewCoverage newCoverage = sensorContext.newCoverage();
    newCoverage.onFile(sourceFile);

    parseValue(srcFile.getTextContent(), newCoverage, sourceFile.lines());
    newCoverage.save();
  }

  private void parseReportFile(SensorContext sensorContext, File reportFile) {
    try {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      docBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      docBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(reportFile);

      // normalize text representation
      doc.getDocumentElement().normalize();

      NodeList dataNodes = doc.getElementsByTagName("linehits");
      Element lineHits = (Element) dataNodes.item(0);
      if (lineHits == null) {
        LOG.warn("'linehits' element not found in coverage report: {}", reportFile);
        return;
      }

      NodeList files = lineHits.getElementsByTagName("file");
      for (int f = 0; f < files.getLength(); f++) {
        parseFileNode(sensorContext, files.item(f));
      }
    } catch (SAXException e) {
      LOG.error("Failed to parse coverage report: ", e);
    } catch (Exception e) {
      LOG.error("Unexpected exception while parsing coverage reports: ", e);
    }
  }

  @Override
  public void parse(SensorContext context, File reportFile) {
    if (!reportFile.exists()) {
      LOG.warn("Report file '{}' does not exist", reportFile);
      return;
    }

    LOG.info("Parsing coverage report: {}", reportFile);
    parseReportFile(context, reportFile);
  }
}
