package org.sonar.plugins.delphi.antlr.ast.xml;

import static org.apache.commons.lang.StringEscapeUtils.escapeXml;

import java.io.File;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
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
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.DelphiPMDNode;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.generated.DelphiParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class DelphiAstSerializer {
  private static final Logger LOG = Loggers.get(DelphiAstSerializer.class);
  private DelphiAST ast;

  private Deque<String> typeNames;
  private Deque<String> methodNames;
  private String unitName;

  public DelphiAstSerializer(DelphiAST ast) {
    this.ast = ast;
  }

  public void dumpXml(String fileName, Document document) {
    Source source = new DOMSource(document);
    File file = new File(fileName);
    Result result = new StreamResult(file);

    try {
      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");

      transformer.transform(source, result);
    } catch (TransformerException e) {
      LOG.error("Failed to serialize AST to XML", e);
    }
  }

  @Nullable
  public Document generateDocument() {
    typeNames = new ArrayDeque<>();
    methodNames = new ArrayDeque<>();
    unitName = "";

    try {
      Document doc = createNewDocument();
      Element root = doc.createElement("file");
      doc.appendChild(root);

      generateNode(root, doc, ast);
      doc.getDocumentElement().normalize();
      return doc;
    } catch (Exception e) {
      LOG.error("{} {}", "Failed to generate Node XML: ", e);
      return null;
    }
  }

  private Document createNewDocument() throws ParserConfigurationException {
    DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    docBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    return docBuilder.newDocument();
  }

  /**
   * Recursively generates XML elements for the whole AST
   *
   * @param root Element
   * @param doc Document
   * @param delphiNode DelphiNode
   */
  private void generateNode(Element root, Document doc, CommonTree delphiNode) {
    if (root == null || doc == null) {
      return;
    }

    handleEnter(delphiNode);

    for (int i = 0; i < delphiNode.getChildCount(); ++i) {
      DelphiPMDNode childNode = new DelphiPMDNode((CommonTree) delphiNode.getChild(i), ast);
      String tag = escapeXml(DelphiParser.tokenNames[childNode.getType()]);

      Element child = doc.createElement(tag);
      child.setTextContent(childNode.getText());
      child.setAttribute("beginLine", String.valueOf(childNode.getBeginLine()));
      child.setAttribute("beginColumn", String.valueOf(childNode.getBeginColumn()));
      child.setAttribute("endLine", String.valueOf(childNode.getBeginLine()));
      child.setAttribute("endColumn", String.valueOf(childNode.getBeginColumn()));
      child.setAttribute("class", getTypeName());
      child.setAttribute("method", getMethodName());
      child.setAttribute("package", getUnitName());
      child.setAttribute("type", String.valueOf(childNode.getType()));

      root.appendChild(child);
      generateNode(child, doc, childNode);
    }

    handleExit(delphiNode);
  }

  private void handleEnter(CommonTree node) {
    switch (node.getType()) {
      case DelphiLexer.LIBRARY:
      case DelphiLexer.PACKAGE:
      case DelphiLexer.PROGRAM:
        handleEnterProjectName(node);
        break;

      case DelphiLexer.UNIT:
        handleEnterUnit(node);
        break;

      case DelphiLexer.TkNewTypeName:
        handleEnterType(node);
        break;

      default:
        if (isMethodHeader(node)) {
          handleEnterMethod(node);
        }
    }
  }

  private void handleExit(CommonTree node) {
    int type = node.getType();

    if (type == DelphiLexer.TkNewType) {
      handleExitType();
    }

    if (isMethodBody(node) && isCurrentMethod(node)) {
      handleExitMethod();
    }
  }

  private void handleEnterUnit(CommonTree node) {
    unitName = extractName(node);
  }

  private void handleEnterProjectName(CommonTree node) {
    unitName = readProjectName(node);
  }

  private void handleEnterType(CommonTree node) {
    typeNames.add(node.getChild(0).getText());
  }

  private void handleEnterMethod(CommonTree node) {
    CommonTree nameNode = (CommonTree) node.getFirstChildWithType(DelphiLexer.TkFunctionName);

    String methodName = extractName(nameNode);

    int dotIndex = methodName.lastIndexOf('.');
    String typeName = "";

    if (dotIndex > 0) {
      typeName = methodName.substring(0, dotIndex);
      methodName = methodName.substring(dotIndex + 1);
    }

    if (!typeName.isEmpty()) {
      typeNames.add(typeName);
    }

    methodNames.add(methodName);
  }

  private void handleExitType() {
    typeNames.removeLast();
  }

  private void handleExitMethod() {
    methodNames.removeLast();

    if (!typeNames.isEmpty()) {
      typeNames.removeLast();
    }
  }

  private String extractName(CommonTree node) {
    List<?> children = node.getChildren();
    return children.stream()
        .map(child -> (Tree) child)
        .map(Tree::getText)
        .collect(Collectors.joining());
  }

  private String readProjectName(CommonTree node) {
    CommonTree parent = (CommonTree) node.getParent();
    Tree firstNode = parent.getChild(node.getChildIndex() + 1);
    StringBuilder nameBuilder = new StringBuilder();
    int i = firstNode.getChildIndex();

    while (++i < parent.getChildCount()) {
      Tree dot = parent.getChild(i);
      if (dot.getType() != DelphiLexer.DOT) {
        break;
      }

      Tree name = parent.getChild(++i);
      nameBuilder.append(".");
      nameBuilder.append(name.getText());
    }

    return nameBuilder.toString();
  }

  private String getTypeName() {
    return buildQualifiedIdent(typeNames);
  }

  private String getMethodName() {
    return buildQualifiedIdent(methodNames);
  }

  private String getUnitName() {
    return unitName;
  }

  private static String buildQualifiedIdent(Deque<String> names) {
    StringBuilder name = new StringBuilder();

    for (String methodName : names) {
      if (name.length() > 0) {
        name.append(".");
      }
      name.append(methodName);
    }

    return name.toString();
  }

  private static boolean isMethodHeader(CommonTree node) {
    return isMethodType(node.getType()) && hasMethodName(node) && hasMethodBody(node);
  }

  private static boolean hasMethodName(CommonTree node) {
    return node.getFirstChildWithType(DelphiLexer.TkFunctionName) != null;
  }

  private static boolean hasMethodBody(CommonTree node) {
    Tree parent = node.getParent();
    int childIndex = node.getChildIndex();

    if (childIndex == parent.getChildCount() - 1) {
      return false;
    }

    int nextNodeType = parent.getChild(childIndex + 1).getType();
    return nextNodeType == DelphiLexer.TkBlockDeclSection;
  }

  private static boolean isMethodBody(CommonTree node) {
    int type = node.getType();
    if (type != DelphiLexer.BEGIN && type != DelphiLexer.ASM) {
      return false;
    }

    int childIndex = node.getChildIndex();
    if (childIndex == 0) {
      return false;
    }

    int prevNodeType = node.getParent().getChild(childIndex - 1).getType();
    return prevNodeType == DelphiLexer.TkBlockDeclSection;
  }

  private boolean isCurrentMethod(CommonTree node) {
    int childIndex = node.getChildIndex();

    CommonTree header = (CommonTree) node.getParent().getChild(childIndex - 2);
    CommonTree nameNode = (CommonTree) header.getFirstChildWithType(DelphiLexer.TkFunctionName);

    if (nameNode == null || methodNames.isEmpty()) {
      return false;
    }

    String methodName = nameNode.getChild(nameNode.getChildCount() - 1).getText();
    String currentMethod = methodNames.getLast();

    return methodName.equals(currentMethod);
  }

  private static boolean isMethodType(int type) {
    return type == DelphiLexer.FUNCTION
        || type == DelphiLexer.PROCEDURE
        || type == DelphiLexer.CONSTRUCTOR
        || type == DelphiLexer.DESTRUCTOR;
  }
}
