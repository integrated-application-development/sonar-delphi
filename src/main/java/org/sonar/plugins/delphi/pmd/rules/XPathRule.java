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
package org.sonar.plugins.delphi.pmd.rules;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import net.sourceforge.pmd.RuleContext;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class XPathRule extends DelphiRule {
  private static final Logger LOG = Loggers.get(XPathRule.class);

  @Override
  public void visitFile(DelphiAST ast, RuleContext ctx) {
    String xPathString = getXPathExpression();

    if (StringUtils.isEmpty(xPathString)) {
      LOG.error("Skipped empty XPath expression in XPathRule: {}.", getName());
      return;
    }

    Document doc = ast.generateDocument();

    try {
      XPath xPath = XPathFactory.newInstance().newXPath();
      XPathExpression expression = xPath.compile(xPathString);
      NodeList nodes = (NodeList) expression.evaluate(doc, XPathConstants.NODESET);

      for (int i = 0; i < nodes.getLength(); ++i) {
        Node resultNode = nodes.item(i);
        NamedNodeMap attributes = resultNode.getAttributes();

        String className = attributes.getNamedItem("class").getTextContent();
        String methodName = attributes.getNamedItem("method").getTextContent();
        String packageName = attributes.getNamedItem("package").getTextContent();
        int beginLine = Integer.parseInt(attributes.getNamedItem("beginLine").getTextContent());
        int beginColumn = Integer.parseInt(attributes.getNamedItem("beginColumn").getTextContent());
        int endLine = Integer.parseInt(attributes.getNamedItem("endLine").getTextContent());
        int endColumn = Integer.parseInt(attributes.getNamedItem("endColumn").getTextContent());

        newViolation(ctx)
            .logicalLocation(packageName, className, methodName)
            .fileLocation(beginLine, beginColumn, endLine, endColumn)
            .message(getViolationMessage())
            .save();
      }
    } catch (Exception e) {
      LOG.warn("{}: XPath error: '{}' at rule {}", ast.getFileName(), e.getMessage(), getName(), e);
    }
  }

  protected abstract String getXPathExpression();

  protected abstract String getViolationMessage();
}
