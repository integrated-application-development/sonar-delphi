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
package org.sonar.plugins.delphi.debug;

import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.IOException;

/**
 * Simple class for parsing XML files in DOM manner
 */
public class SimpleXMLParser {

  /**
   * Parsers XML file and returns root node
   * 
   * @param xmlFile XML file to parse
   * @return Root XML node
   */
  public static Document parseXML(File xmlFile) {
    try {
      DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
      DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
      Document doc = dBuilder.parse(xmlFile);
      doc.getDocumentElement().normalize();
      return doc;
    } catch (IOException ioe) {
      DelphiUtils.LOG.error("Error parsing file: " + ioe.getMessage());
      return null;
    } catch (Exception e) {
      DelphiUtils.LOG.error("XML Error: " + e.getMessage());
      return null;
    }
  }

  /**
   * Gets node value
   * 
   * @param node Node
   * @return Node value
   */
  public static String getNodeValueText(Node node) {
    return node.getChildNodes().item(0).getNodeValue();
  }

  /**
   * Gets nodes from current node by name
   * 
   * @param node Current node
   * @param tag Name of child nodes
   * @return List of child nodes with specified tag
   */
  public static NodeList getValueNodes(Node node, String tag) {
    if (node.getNodeType() != Node.ELEMENT_NODE) {
      return null;
    }
    Element el = (Element) node;
    return el.getElementsByTagName(tag);
  }
}
