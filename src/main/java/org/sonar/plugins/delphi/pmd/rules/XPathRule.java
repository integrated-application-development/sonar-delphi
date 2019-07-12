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

import java.util.List;
import net.sourceforge.pmd.RuleContext;
import net.sourceforge.pmd.properties.PropertyDescriptor;
import net.sourceforge.pmd.properties.PropertyFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.xml.dtm.DTM;
import org.apache.xml.dtm.DTMIterator;
import org.apache.xpath.XPathAPI;
import org.apache.xpath.objects.XNodeSet;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.ast.ASTTree;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * DelphiLanguage rule for XPath, use it to parse XPath rules
 */
public class XPathRule extends DelphiRule {
  private static final Logger LOG = Loggers.get(XPathRule.class);

  private static final PropertyDescriptor<String> XPATH = PropertyFactory
      .stringProperty("xpath")
      .desc("The xpath expression")
      .defaultValue("")
      .build();

  /**
   * Last cached document.
   */
  private static Document cachedData;
  /**
   * Last cached file name.
   */
  private static String cachedFile = "";

  public XPathRule() {
    definePropertyDescriptor(XPATH);
  }

  @Override
  public void visit(DelphiPMDNode node, RuleContext ctx) {
    String xPathString = getProperty(XPATH);
    if (StringUtils.isEmpty(xPathString)) {
      return;
    }
    Document doc = getCachedDocument(node.getASTTree());
    try {
      XNodeSet result = (XNodeSet) XPathAPI.eval(doc, xPathString);

      final DTMIterator iterator = result.iter();

      while (iterator.nextNode() != DTM.NULL) {
        final int nodeId = iterator.getCurrentNode();
        Node resultNode = iterator.getDTM(nodeId).getNode(nodeId);
        NamedNodeMap attributes = resultNode.getAttributes();

        String className = attributes.getNamedItem("class").getTextContent();
        String methodName = attributes.getNamedItem("method").getTextContent();
        String packageName = attributes.getNamedItem("package").getTextContent();
        int beginLine = Integer.parseInt(attributes.getNamedItem("beginLine").getTextContent());
        int beginColumn = Integer.parseInt(attributes.getNamedItem("beginColumn").getTextContent());
        int endLine = Integer.parseInt(attributes.getNamedItem("endLine").getTextContent());
        int endColumn = Integer.parseInt(attributes.getNamedItem("endColumn").getTextContent());

        String codeLine = node.getASTTree().getFileSourceLine(beginLine).trim();

        if (codeLine.endsWith("//NOSONAR")) {
          continue;
        }

        String msg = this.getMessage().replaceAll("\\{}", resultNode.getTextContent());

        newViolation(ctx)
            .logicalLocation(packageName, className, methodName)
            .fileLocation(beginLine, beginColumn, endLine, endColumn)
            .message(msg)
            .save();
      }
    } catch (Exception e) {
      LOG.warn("{}: XPath error: '{}' at rule {}",
          node.getASTTree().getFileName(), e.getMessage(), getName(), e);
    }
  }

  /**
   * Preform only one visit per file, not per node cause we parse the whole file nodes at a time
   */

  @Override
  protected void visitAll(List<? extends net.sourceforge.pmd.lang.ast.Node> acus, RuleContext ctx) {
    init();
    if (acus.iterator().hasNext()) {
      visit((DelphiPMDNode) acus.iterator().next(), ctx);
    }
  }

  /**
   * Gets the cached AST document, create new if not found in cache
   *
   * @param astTree AST tree
   * @return AST tree document
   */
  private static Document getCachedDocument(ASTTree astTree) {
    if (!astTree.getFileName().equals(cachedFile)) {
      cachedData = astTree.generateDocument();
      cachedFile = astTree.getFileName();
    }
    return cachedData;
  }

}
