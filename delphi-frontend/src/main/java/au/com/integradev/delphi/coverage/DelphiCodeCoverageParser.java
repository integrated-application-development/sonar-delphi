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
package au.com.integradev.delphi.coverage;

import au.com.integradev.delphi.msbuild.DelphiProjectHelper;
import com.google.common.base.Splitter;
import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableSortedMap;
import java.io.File;
import java.util.Map;
import java.util.function.Supplier;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class DelphiCodeCoverageParser implements DelphiCoverageParser {
  private static final Logger LOG = LoggerFactory.getLogger(DelphiCodeCoverageParser.class);
  private final DelphiProjectHelper delphiProjectHelper;
  private final Supplier<Map<String, InputFile>> fileNameToInputFile;

  public DelphiCodeCoverageParser(DelphiProjectHelper delphiProjectHelper) {
    this.delphiProjectHelper = delphiProjectHelper;
    this.fileNameToInputFile = Suppliers.memoize(this::indexInputFiles);
  }

  private Map<String, InputFile> indexInputFiles() {
    var builder = ImmutableSortedMap.<String, InputFile>orderedBy(String.CASE_INSENSITIVE_ORDER);
    for (InputFile inputFile : delphiProjectHelper.inputFiles()) {
      builder.put(inputFile.filename(), inputFile);
    }
    return builder.build();
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

  private void parseReportFile(SensorContext sensorContext, File reportFile) {
    try {
      DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
      docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      docBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
      docBuilderFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(reportFile);

      doc.getDocumentElement().normalize();

      NodeList dataNodes = doc.getElementsByTagName("linehits");
      Element lineHits = (Element) dataNodes.item(0);
      if (lineHits == null) {
        LOG.warn("'linehits' element not found in coverage report: {}", reportFile);
        return;
      }

      NodeList files = lineHits.getElementsByTagName("file");
      for (int i = 0; i < files.getLength(); i++) {
        parseFileNode(sensorContext, files.item(i));
      }
    } catch (SAXException e) {
      LOG.error("Failed to parse coverage report: ", e);
    } catch (Exception e) {
      LOG.error("Unexpected exception while parsing coverage reports: ", e);
    }
  }

  private void parseFileNode(SensorContext sensorContext, Node srcFile) {
    String fileName = srcFile.getAttributes().getNamedItem("name").getTextContent();
    InputFile sourceFile = fileNameToInputFile.get().get(fileName);
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

  private static void parseValue(String lineCoverage, NewCoverage newCoverage, int numLines) {
    Splitter.on(';')
        .split(lineCoverage)
        .forEach(lineHit -> parseLineHit(lineHit, numLines, newCoverage));
  }

  private static void parseLineHit(String lineHit, int numLines, NewCoverage newCoverage) {
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
}
