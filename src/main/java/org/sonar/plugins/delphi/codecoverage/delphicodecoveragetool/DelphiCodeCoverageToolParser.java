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

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.plugins.delphi.codecoverage.DelphiCodeCoverageParser;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DelphiCodeCoverageToolParser implements DelphiCodeCoverageParser {

  private final File reportFile;
  private final DelphiProjectHelper delphiProjectHelper;

  public DelphiCodeCoverageToolParser(File reportFile, DelphiProjectHelper delphiProjectHelper) {
    this.reportFile = reportFile;
    this.delphiProjectHelper = delphiProjectHelper;
  }

  public static boolean isCodeCoverageReport(Path path) {
    return "CodeCoverage_Summary.xml".equalsIgnoreCase(path.getFileName().toString());
  }

  private void parseLineHit(String lineCoverage, int startPos, int endPos,
      NewCoverage newCoverage) {
    String lineHit = lineCoverage.substring(startPos, endPos);
    int eq = lineHit.indexOf('=');
    if (eq > 0) {
      int lineNumber = Integer.parseInt(lineHit.substring(0, eq));
      int lineHits = Integer.parseInt(lineHit.substring(eq + 1));
      newCoverage.lineHits(lineNumber, lineHits);
    }
  }

  private void parseValue(String lineCoverage, NewCoverage newCoverage) {
    int pos = 0;
    int end;
    while ((end = lineCoverage.indexOf(';', pos)) >= 0) {
      parseLineHit(lineCoverage, pos, end, newCoverage);
      pos = end + 1;
    }
    if (lineCoverage.length() - 1 > pos) {
      parseLineHit(lineCoverage, pos, lineCoverage.length(), newCoverage);
    }
  }

  private void parseFileNode(SensorContext sensorContext, Node srcFile) {
    String fileName = srcFile.getAttributes().getNamedItem("name").getTextContent();
    try {
      InputFile sourceFile = delphiProjectHelper.findFileInDirectories(fileName);
      NewCoverage newCoverage = sensorContext.newCoverage();
      newCoverage.onFile(sourceFile);

      parseValue(srcFile.getTextContent(), newCoverage);
      newCoverage.save();
    } catch (FileNotFoundException e) {
      DelphiUtils.LOG.info("File not found in project {}", fileName);
      DelphiUtils.LOG.debug("Stacktrace: ", e);
    }

  }

  private void parseReportFile(SensorContext sensorContext) {
    try {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(reportFile);

      // normalize text representation
      doc.getDocumentElement().normalize();

      NodeList dataNodes = doc.getElementsByTagName("linehits");
      Element lineHits = (Element) dataNodes.item(0);
      NodeList files = lineHits.getElementsByTagName("file");

      for (int f = 0; f < files.getLength(); f++) {
        Node srcFile = files.item(f);
        parseFileNode(sensorContext, srcFile);
      }

    } catch (SAXException e) {
      DelphiUtils.LOG.error("Failed to parse coverage report: ", e);
    } catch (Exception e) {
      DelphiUtils.LOG.error("Unexpected exception while parsing coverage reports: ", e);
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
