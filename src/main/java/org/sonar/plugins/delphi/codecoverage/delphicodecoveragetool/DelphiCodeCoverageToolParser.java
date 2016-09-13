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

import org.sonar.api.batch.SensorContext;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.delphi.codecoverage.DelphiCodeCoverageParser;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;

public class DelphiCodeCoverageToolParser implements DelphiCodeCoverageParser
{
  private final File reportFile;
  private final DelphiProjectHelper delphiProjectHelper;

  public DelphiCodeCoverageToolParser(File reportFile, DelphiProjectHelper delphiProjectHelper) {
    this.reportFile = reportFile;
    this.delphiProjectHelper = delphiProjectHelper;
  }

  @Override
  public void parse(SensorContext context) {
    if (!reportFile.exists()) {
      return;
    }

    try {
      StaxParser parser = new StaxParser(new DelphiCoverageToolParserStreamHandler(context, delphiProjectHelper));
      parser.parse(reportFile);
    } catch (XMLStreamException e) {
      DelphiUtils.LOG.error("Error parsing file : {}", reportFile);
    }
  }
}
