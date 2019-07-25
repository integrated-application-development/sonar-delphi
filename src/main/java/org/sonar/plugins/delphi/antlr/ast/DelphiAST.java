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

import static org.apache.commons.lang.StringEscapeUtils.escapeXml;

import java.io.File;
import java.io.IOException;
import java.util.List;
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
import org.apache.commons.io.FileUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStream;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStreamConfig;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.generated.DelphiParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** DelphiLanguage AST tree. */
public class DelphiAST extends CommonTree implements ASTTree {
  private static final Logger LOG = Loggers.get(DelphiAST.class);

  private String fileName;
  private boolean isError;
  private DelphiFileStream fileStream;
  private List<String> codeLines;

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
      codeLines = FileUtils.readLines(file, fileStreamConfig.getEncoding());
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
  }

  /** Empty, default c-tor. */
  public DelphiAST() {}

  /** {@inheritDoc} */
  @Override
  public String getFileName() {
    return fileName;
  }

  /** {@inheritDoc} */
  @Override
  public boolean isError() {
    return isError;
  }

  /** {@inheritDoc} */
  @Override
  public void generateXML(String fileName) {
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
      LOG.error("Failed to generate Node XML", e);
    }
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
      LOG.error("{} {}", "Failed to generate Node XML: ", e);
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
      DelphiPMDNode childNode = new DelphiPMDNode((CommonTree) delphiNode.getChild(i), this);
      String tag = escapeXml(DelphiParser.tokenNames[childNode.getType()]);

      Element child = doc.createElement(tag);
      child.setTextContent(childNode.getText());
      child.setAttribute("beginLine", String.valueOf(childNode.getBeginLine()));
      child.setAttribute("beginColumn", String.valueOf(childNode.getBeginColumn()));
      child.setAttribute("endLine", String.valueOf(childNode.getBeginLine()));
      child.setAttribute("endColumn", String.valueOf(childNode.getBeginColumn()));
      child.setAttribute("class", "");
      child.setAttribute("method", "");
      child.setAttribute("package", "");
      child.setAttribute("type", String.valueOf(childNode.getType()));

      root.appendChild(child);
      generateDocumentChildren(child, doc, childNode);
    }
  }

  /** {@inheritDoc} */
  @Override
  public String getFileSource() {
    return fileStream.toString();
  }

  @Override
  public String getFileSourceLine(int line) {
    if (line < 1) {
      throw new IllegalArgumentException(toString() + " Source code line cannot be less than 1");
    }
    if (line > codeLines.size()) {
      throw new IllegalArgumentException(
          toString() + "Source code line number too high: " + line + " / " + codeLines.size());
    }
    return codeLines.get(line - 1);
  }

  @Override
  public String toString() {
    return "DelphiAST{" + "fileName='" + fileName + '\'' + '}';
  }
}
