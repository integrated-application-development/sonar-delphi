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
import org.sonar.api.batch.sensor.coverage.NewCoverage;
import org.sonar.plugins.delphi.codecoverage.DelphiCodeCoverageParser;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import java.io.File;
import java.io.FileNotFoundException;

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

  private void parseLineHit(String lineCoverage, int startPos, int endPos, NewCoverage newCoverage)
  {
    String lineHit = lineCoverage.substring(startPos, endPos);
    int eq = lineHit.indexOf('=');
    if (eq > 0) {
      int lineNumber = Integer.parseInt(lineHit.substring(0, eq));
      int lineHits = Integer.parseInt(lineHit.substring(eq + 1));
      newCoverage.lineHits(lineNumber, lineHits);
    }
  }

  private void parseValue(String lineCoverage, NewCoverage newCoverage)
  {
    int pos = 0, end;
    while ((end = lineCoverage.indexOf(';', pos)) >= 0) {
      parseLineHit(lineCoverage, pos, end, newCoverage);
      pos = end + 1;
    }
    if (lineCoverage.length() - 1 > pos) {
      parseLineHit(lineCoverage, pos, lineCoverage.length(), newCoverage);
    }
  }

  private void parseReportFile(SensorContext sensorContext)
  {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    try {
      DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
      Document doc = docBuilder.parse(reportFile);

      // normalize text representation
      doc.getDocumentElement().normalize();

      NodeList dataNodes = doc.getElementsByTagName("linehits");
      Element lineHits = (Element)(dataNodes.item(0));
      NodeList files = lineHits.getElementsByTagName("file");

      for (int f = 0; f < files.getLength(); f++) {
        Node srcFile = files.item(f);
        String fileName = srcFile.getAttributes().getNamedItem("name").getTextContent();

        try {
          InputFile sourceFile = delphiProjectHelper.findFileInDirectories(fileName);
          NewCoverage newCoverage = sensorContext.newCoverage();
          newCoverage.onFile(sourceFile);

          parseValue(srcFile.getTextContent(), newCoverage);
          newCoverage.save();
        }
        catch (FileNotFoundException e) {
          DelphiUtils.LOG.info("File not found in project" + fileName);
        }
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
