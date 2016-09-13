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
package org.sonar.plugins.delphi.pmd.xml;

import org.apache.commons.lang.StringUtils;
import org.codehaus.staxmate.in.SMHierarchicCursor;
import org.codehaus.staxmate.in.SMInputCursor;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.component.ResourcePerspectives;
import org.sonar.api.issue.Issuable;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.utils.StaxParser;
import org.sonar.plugins.delphi.core.helpers.DelphiProjectHelper;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.utils.DelphiUtils;

import javax.xml.stream.XMLStreamException;
import java.io.File;

/**
 * Parses PMD xml report
 */
public class DelphiPmdXmlReportParser {

  private final DelphiProjectHelper delphiProjectHelper;
  private final ResourcePerspectives perspectives;

  public DelphiPmdXmlReportParser(DelphiProjectHelper delphiProjectHelper, ResourcePerspectives perspectives) {
    this.delphiProjectHelper = delphiProjectHelper;
    this.perspectives = perspectives;
  }

  /**
   * Parses XML file
   * 
   * @param xmlFile PMD xml file
   */
  public void parse(File xmlFile) {
    StaxParser parser = new StaxParser(new StaxParser.XmlStreamHandler() {

      @Override
      public void stream(SMHierarchicCursor rootCursor) throws XMLStreamException {
        rootCursor.advance();

        SMInputCursor fileCursor = rootCursor.descendantElementCursor("file");
        while (fileCursor.getNext() != null) {
          String fileName = fileCursor.getAttrValue("name");

          SMInputCursor violationCursor = fileCursor.descendantElementCursor("violation");
          while (violationCursor.getNext() != null) {
            String beginLine = violationCursor.getAttrValue("beginline");
            //String endLine = violationCursor.getAttrValue("beginline");
            String ruleKey = violationCursor.getAttrValue("rule");
            String message = StringUtils.trim(violationCursor.collectDescendantText());
            System.out.println("HIER parse1:"+ruleKey+message+beginLine);
            addIssue(ruleKey, fileName, Integer.parseInt(beginLine), message);
          }
        }
      }
    });

    try {
      parser.parse(xmlFile);
    } catch (XMLStreamException e) {
      DelphiUtils.LOG.error("Error parsing file : {}", xmlFile);
    }
  }

  private void addIssue(String ruleKey, String fileName, Integer beginLine, String message) {

    DelphiUtils.LOG.debug("PMD Violation - rule: " + ruleKey + " file: " + fileName + " message: " + message);

    InputFile inputFile = delphiProjectHelper.getFile(fileName);
    Issuable issuable = perspectives.as(Issuable.class, inputFile);
    if (issuable != null) {
                //note this has been added to get compatibility with sonar 5.2
        issuable.addIssue(
                issuable.newIssueBuilder()
                        .ruleKey(RuleKey.of(DelphiPmdConstants.REPOSITORY_KEY, ruleKey))
                        .effortToFix(0.0)
                        .message(message)
                        .build()
        );
        //System.out.println("ISSUE TOSTRING: "+issue.toString());
    }

  }
}
