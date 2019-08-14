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

import com.qualinsight.plugins.sonarqube.smell.api.annotation.Smell;
import com.qualinsight.plugins.sonarqube.smell.api.model.SmellType;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.antlr.runtime.BufferedTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenRewriteStream;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;
import org.antlr.runtime.tree.TreeAdaptor;
import org.apache.commons.io.FileUtils;
import org.sonar.plugins.delphi.antlr.ast.xml.DelphiAstSerializer;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStream;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStreamConfig;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.antlr.generated.DelphiParser;
import org.w3c.dom.Document;

/** DelphiLanguage AST tree. */
public class DelphiAST extends CommonTree implements ASTTree {
  private String fileName;
  private boolean isError;
  private DelphiFileStream fileStream;
  private List<Token> comments;
  private List<String> codeLines;
  private Document document;

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

    TokenRewriteStream tokenStream = new TokenRewriteStream(new DelphiLexer(fileStream));
    comments = extractComments(tokenStream);

    DelphiParser parser = new DelphiParser(tokenStream);
    TreeAdaptor adaptor = new DelphiTreeAdaptor(this);
    parser.setTreeAdaptor(adaptor);

    try {
      children = ((CommonTree) parser.file().getTree()).getChildren();
    } catch (RecognitionException e) {
      throw new FileParseFailException("Failed to parse the file " + file.getAbsolutePath(), e);
    }

    fileName = file.getAbsolutePath();
    isError = parser.getNumberOfSyntaxErrors() != 0;
  }

  @Smell(
      minutes = 120,
      reason =
          "This constructor only exists for tests, and doesn't make much sense."
              + " The contract of DelphiAST appears to be that you feed it a file,"
              + " and it populates itself with nodes."
              + " Using this empty constructor and then trying to actually use the object "
              + " would only yield NPEs.",
      type = SmellType.BAD_DESIGN)
  public DelphiAST() {}

  private List<Token> extractComments(BufferedTokenStream tokenStream) {
    List<?> tokens = tokenStream.getTokens();
    return tokens.stream()
        .map(token -> (Token) token)
        .filter(token -> token.getType() == DelphiLexer.COMMENT)
        .collect(Collectors.toList());
  }

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
    DelphiAstSerializer serializer = new DelphiAstSerializer(this);
    serializer.dumpXml(fileName, generateDocument());
  }

  /** {@inheritDoc} */
  @Override
  @Nullable
  public Document generateDocument() {
    if (document == null) {
      DelphiAstSerializer serializer = new DelphiAstSerializer(this);
      document = serializer.generateDocument();
    }

    return document;
  }

  /** {@inheritDoc} */
  @Override
  public String getFileSource() {
    return fileStream.toString();
  }

  /** {@inheritDoc} */
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

  public List<Token> getComments() {
    return comments;
  }

  private List<Token> getCommentsBetweenTokenIndices(int startIndex, int endIndex) {
    return comments.stream()
        .filter(
            token -> {
              int index = token.getTokenIndex();
              return index > startIndex && index < endIndex;
            })
        .collect(Collectors.toList());
  }

  public List<Token> getCommentsInsideNode(Tree node) {
    return getCommentsBetweenTokenIndices(node.getTokenStartIndex(), node.getTokenStopIndex());
  }

  @Override
  public String toString() {
    return "DelphiAST{" + "fileName='" + fileName + '\'' + '}';
  }
}
