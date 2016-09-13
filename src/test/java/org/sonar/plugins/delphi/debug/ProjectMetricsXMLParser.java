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

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.util.*;

/**
 * Class for parsing value.xml file, used for DelphiSensorTest
 */
public class ProjectMetricsXMLParser extends SimpleXMLParser {

  private Map<String, Node> fileMap = null;

  public ProjectMetricsXMLParser(File xmlFile) {
    if (xmlFile == null) {
      throw new IllegalArgumentException("xmlFile cannot be null.");
    }
    fileMap = new HashMap<String, Node>();
    Document doc = parseXML(xmlFile);
    NodeList filesNode = doc.getElementsByTagName("file");
    parse(filesNode);
  }

  // parse file for "file" node
  private void parse(NodeList filesNode) {
    for (int i = 0; i < filesNode.getLength(); ++i) // for all files
    {
      Node file = filesNode.item(i); // get file
      String fileName = getNodeValueText(getValueNodes(file, "name").item(0)); // get
                                                                               // its
                                                                               // "name"
                                                                               // node
      fileMap.put(fileName, file); // put to map
    }

  }

  /**
   * Gets the name of all files to check in DelphiSensorTest class
   * 
   * @return Set of file names (set of Strings)
   */
  public Set<String> getFileNames() {
    return fileMap.keySet();
  }

  /**
   * Gets expected metric values for specified file
   * 
   * @param filename File name
   * @return Expected values, array of doubles
   */
  public Double[] getFileValues(String filename) {

    if (!fileMap.containsKey(filename)) {
      return null;
    }
    NodeList att = getValueNodes(fileMap.get(filename), "metric");
    List<Double> data = new ArrayList<Double>();

    for (int i = 0; i < att.getLength(); ++i) {
      Node metric = att.item(i);
      data.add(Double.valueOf(getNodeValueText(metric)));
    }

    return data.toArray(new Double[data.size()]);
  }

}
