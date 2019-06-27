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
package org.sonar.plugins.delphi.antlr.ast;

import java.io.File;
import java.io.IOException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
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
import org.sonar.plugins.delphi.DelphiPlugin;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.generated.DelphiParser;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStream;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStreamConfig;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * DelphiLanguage AST tree.
 */
public class DelphiAST extends CommonTree implements ASTTree {

  private String fileName;
  private boolean isError;
  private DelphiFileStream fileStream;
  private String[] codeLines;

  private static class FileReadFailException extends RuntimeException {

    FileReadFailException(String s, IOException e) {
      super(s, e);
    }
  }

  private static class FileParseFailException extends RuntimeException {

    FileParseFailException(String s, RecognitionException e) {
      super(s, e);
    }
  }

  /**
   * Constructor with default values for fileStreamConfig. Used for tests
   *
   * @param file Input file from which to read data for AST tree
   */
  public DelphiAST(File file) {
    this(file, new DelphiFileStreamConfig());
  }

  /**
   * Constructor.
   *
   * @param file Input file from which to read data for AST tree
   * @param fileStreamConfig Configures the DelphiFileStream which is fed to the lexer
   */
  public DelphiAST(File file, DelphiFileStreamConfig fileStreamConfig) {
    try {
      fileStream = new DelphiFileStream(file.getAbsolutePath(), fileStreamConfig);
    } catch (IOException e) {
      throw new FileReadFailException("Failed to read file " + file.getAbsolutePath(), e);
    }
    DelphiParser parser = new DelphiParser(new TokenRewriteStream(new DelphiLexer(fileStream)));
    parser.setTreeAdaptor(new DelphiTreeAdaptor(this));
    try {
      children = ((CommonTree) parser.file().getTree()).getChildren();

    } catch (RecognitionException e) {
      throw new FileParseFailException("Failed to parse the file " + file.getAbsolutePath(), e);
    }
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

  @Override
  public String getFileName() {
    return fileName;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public boolean isError() {
    return isError;
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public File generateXML(String fileName) {
    Source source = new DOMSource(generateDocument());

    File file = new File(fileName);
    Result result = new StreamResult(file);

    try {
      // Write the DOM document to the file
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      transformer.transform(source, result);
    } catch (TransformerException e) {
      DelphiPlugin.LOG.error("Failed to generate Node XML", e);
      return null;
    }

    return file;
  }

  /**
   * Generates an XML document from current node
   *
   * @return XML document
   */

  @Override
  public Document generateDocument() {
    try {
      return generateDocument(createNewDocument(), "file");
    } catch (Exception e) {
      DelphiPlugin.LOG.error("{} {}", "Failed to generate Node XML: ", e);
      return null;
    }
  }

  private Document generateDocument(Document doc, String rootName) {
    Element root = doc.createElement(rootName);
    doc.appendChild(root);

    // create root children, and their children, and so on
    generateDocumentChildren(root, doc, this);
    doc.getDocumentElement().normalize();
    return doc;
  }

  private Document createNewDocument() throws ParserConfigurationException {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    return docBuilder.newDocument();
  }

  /**
   * Generate children for specified root element from delphi node
   *
   * @param root Element
   * @param doc Document
   * @param delphiNode DelphiNode
   */
  private void generateDocumentChildren(Element root, Document doc, Tree delphiNode) {
    if (root == null || doc == null) {
      return;
    }

    for (int i = 0; i < delphiNode.getChildCount(); ++i) {
      Tree childNode = delphiNode.getChild(i);
      String processedName = processNodeName(childNode);
      Element child = doc.createElement(processedName);
      child.setTextContent(childNode.getText());
      child.setAttribute("line", String.valueOf(childNode.getLine()));
      child.setAttribute("column", String.valueOf(childNode.getCharPositionInLine()));
      child.setAttribute("class", "");
      child.setAttribute("method", "");
      child.setAttribute("package", "");
      child.setAttribute("type", String.valueOf(childNode.getType()));
      root.appendChild(child);
      generateDocumentChildren(child, doc, childNode);
    }
  }

  private String processNodeName(Tree node) {
    return NodeName.findByCode(node.getText()).getName();
  }

  /**
   * {@inheritDoc}
   */

  @Override
  public String getFileSource() {
    return fileStream.toString();
  }

  @Override
  public String getFileSourceLine(int lineNr) {
    if (lineNr < 1) {
      throw new IllegalArgumentException(toString() + " Source code line cannot be less than 1");
    }
    if (lineNr > codeLines.length) {
      throw new IllegalArgumentException(
          toString() + "Source code line number too high: " + lineNr + " / " + codeLines.length);
    }
    return codeLines[lineNr - 1];
  }

  @Override
  public String toString() {
    return "DelphiAST{" + "fileName='" + fileName + '\'' + '}';
  }
}
