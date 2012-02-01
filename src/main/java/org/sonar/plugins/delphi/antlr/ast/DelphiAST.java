/*
 * Sonar Delphi Plugin
 * Copyright (C) 2011 Sabre Airline Solutions
 * Author(s):
 * Przemyslaw Kociolek (przemyslaw.kociolek@sabre.com)
 * Michal Wojcik (michal.wojcik@sabre.com)
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
package org.sonar.plugins.delphi.antlr.ast;

import java.io.File;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenRewriteStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.DelphiParser;
import org.sonar.plugins.delphi.antlr.ast.exceptions.NodeNameForCodeDoesNotExistException;
import org.sonar.plugins.delphi.antlr.sanitizer.DelphiSourceSanitizer;
import org.sonar.plugins.delphi.utils.DelphiUtils;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * DelphiLanguage AST tree.
 */
public class DelphiAST extends CommonTree implements ASTTree {

  private String fileName = null;
  private boolean isError = false;
  private DelphiSourceSanitizer fileStream = null;
  private String[] codeLines = null;

  /**
   * Constructor.
   * 
   * @param inputStream
   *          Input from which to read data for AST tree
   * @throws RecognitionException
   *           At parsing exception
   * @throws IOException
   *           When no file found
   */
  public DelphiAST(File file) throws IOException, RecognitionException {
    fileStream = new DelphiSourceSanitizer(file.getAbsolutePath());
    DelphiParser parser = new DelphiParser(new TokenRewriteStream(new DelphiLexer(fileStream)));
    parser.setTreeAdaptor(new DelphiTreeAdaptor(this));
    children = ((CommonTree) parser.file().getTree()).getChildren();
    fileName = file.getAbsolutePath();
    isError = parser.getNumberOfSyntaxErrors() != 0;
    codeLines = fileStream.toString().split("\n");
  }

  /**
   * Empty, default c-tor.
   */
  public DelphiAST() {
  }

  /**
   * {@inheritDoc}
   */

  public String getFileName() {
    return fileName;
  }

  /**
   * {@inheritDoc}
   */

  public boolean isError() {
    return isError;
  }

  /**
   * Generates and saves AST tree to XML file
   * 
   * @return XML file
   */

  public File generateXML(String fileName) {
    Source source = new DOMSource(generateDocument());

    // Prepare the output file
    File file = new File(fileName);
    Result result = new StreamResult(file);

    try // Write the DOM document to the file
    {
      Transformer xformer = TransformerFactory.newInstance().newTransformer();
      xformer.transform(source, result);
    } catch (TransformerException e) {
      return null;
    }

    return file;
  }

  /**
   * Generates an XML document from current node
   * 
   * @return XML document
   */

  public Document generateDocument() {
    try {
      return generateDocument(createNewDocument(), "file");
    } catch (Exception e) {
      DelphiUtils.LOG.error(toString() + "Could not generate xml document: " + e.getMessage());
      DelphiUtils.getDebugLog().println(toString() + "Could not generate xml document: " + e.getMessage());
      return null;
    }
  }

  private Document generateDocument(Document doc, String rootName) {
    Element root = doc.createElement(rootName); // create documents root
    doc.appendChild(root);
    generateDocumentChildren(root, doc, this); // create root children, and their children, and so on
    doc.getDocumentElement().normalize(); // normalize document
    return doc;
  }

  private Document createNewDocument() throws ParserConfigurationException {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    return docBuilder.newDocument();
  }

  /**
   * Generate children for specified root element from delphi node
   * 
   * @param root
   *          Element
   * @param doc
   *          Document
   * @param delphiNode
   *          DelphiNode
   */
  protected void generateDocumentChildren(Element root, Document doc, Tree delphiNode) {
    if (root == null || doc == null) {
      return;
    }

    for (int i = 0; i < delphiNode.getChildCount(); ++i) {
      Tree childNode = delphiNode.getChild(i);
      try {
        String processedName = processNodeName(childNode);
        Element child = doc.createElement(processedName);
        child.setTextContent(childNode.getText());
        child.setAttribute("line", String.valueOf(childNode.getLine()));
        child.setAttribute("column", String.valueOf(childNode.getCharPositionInLine()));
        child.setAttribute("class", ""); // not needed
        child.setAttribute("method", ""); // not needed
        child.setAttribute("package", "");
        root.appendChild(child);
        generateDocumentChildren(child, doc, childNode);
      } catch (DOMException e) {
        continue;
      }
    }
  }

  /**
   * Some characters are forbidden as XML node, so process them
   * 
   * @param str
   *          String to process
   * @return Fixed string
   */
  private String processNodeName(Tree node) {
    String code = node.getText();
    try {
      NodeName nodeName = NodeName.findByCode(code);
      return nodeName.getName();
    } catch (NodeNameForCodeDoesNotExistException e) {

    }
    return code;
  }

  /**
   * {@inheritDoc}
   */

  public String getFileSource() {
    return fileStream.toString();
  }

  public String getFileSourceLine(int lineNr) {
    if (lineNr < 1) {
      throw new IllegalArgumentException(toString() + " Source code line cannot be less than 1");
    }
    if (lineNr > codeLines.length) {
      throw new IllegalArgumentException(toString() + "Source code line number to high: " + lineNr + " of max " + codeLines.length);
    }
    return codeLines[lineNr - 1];
  }

  @Override
  public String toString() {
    return "DelphiAST{" + "fileName='" + fileName + '\'' + '}';
  }
}
