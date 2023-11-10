/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.file;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Predicate.not;
import static org.apache.commons.io.FileUtils.readLines;

import au.com.integradev.delphi.antlr.DelphiFileStream;
import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.DelphiParser;
import au.com.integradev.delphi.antlr.DelphiTokenStream;
import au.com.integradev.delphi.antlr.ast.DelphiAstImpl;
import au.com.integradev.delphi.antlr.ast.DelphiTreeAdaptor;
import au.com.integradev.delphi.antlr.ast.token.DelphiTokenImpl;
import au.com.integradev.delphi.antlr.ast.token.IncludeToken;
import au.com.integradev.delphi.preprocessor.CompilerSwitchRegistry;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessor;
import au.com.integradev.delphi.preprocessor.DelphiPreprocessorFactory;
import au.com.integradev.delphi.preprocessor.search.SearchPath;
import au.com.integradev.delphi.utils.DelphiUtils;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.antlr.runtime.BufferedTokenStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.communitydelphi.api.ast.DelphiAst;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public interface DelphiFile {
  File getSourceCodeFile();

  List<String> getSourceCodeFilesLines();

  DelphiAst getAst();

  List<DelphiToken> getTokens();

  List<DelphiToken> getComments();

  CompilerSwitchRegistry getCompilerSwitchRegistry();

  TypeFactory getTypeFactory();

  interface DelphiInputFile extends DelphiFile {
    InputFile getInputFile();

    static DelphiInputFile from(InputFile inputFile, DelphiFileConfig config) {
      DefaultDelphiInputFile delphiFile = new DefaultDelphiInputFile();
      File sourceFile = new File(DelphiUtils.uriToAbsolutePath(inputFile.uri()));
      setupFile(delphiFile, sourceFile, config);
      delphiFile.setInputFile(inputFile);
      return delphiFile;
    }
  }

  class DelphiFileConstructionException extends RuntimeException {
    DelphiFileConstructionException(Throwable cause) {
      super("Failed to construct DelphiFile (" + cause.getMessage() + ")", cause);
    }
  }

  class EmptyDelphiFileException extends RuntimeException {
    EmptyDelphiFileException(String message) {
      super(message);
    }
  }

  static DelphiFileConfig createConfig(
      String encoding,
      DelphiPreprocessorFactory preprocessorFactory,
      TypeFactory typeFactory,
      SearchPath searchPath,
      Set<String> definitions) {
    return createConfig(encoding, preprocessorFactory, typeFactory, searchPath, definitions, false);
  }

  static DelphiFileConfig createConfig(
      @Nullable String encoding,
      DelphiPreprocessorFactory preprocessorFactory,
      TypeFactory typeFactory,
      SearchPath searchPath,
      Set<String> definitions,
      boolean shouldSkipImplementation) {
    return new DefaultDelphiFileConfig(
        encoding,
        preprocessorFactory,
        typeFactory,
        searchPath,
        definitions,
        shouldSkipImplementation);
  }

  static DelphiFile from(File sourceFile, DelphiFileConfig config) {
    DefaultDelphiFile delphiFile = new DefaultDelphiFile();
    setupFile(delphiFile, sourceFile, config);
    return delphiFile;
  }

  static void setupFile(DefaultDelphiFile delphiFile, File sourceFile, DelphiFileConfig config) {
    try {
      delphiFile.setSourceCodeFile(sourceFile);
      delphiFile.setTypeFactory(config.getTypeFactory());
      DelphiPreprocessor preprocessor = preprocess(delphiFile, config);
      delphiFile.setAst(createAST(delphiFile, preprocessor.getTokenStream(), config));
      delphiFile.setCompilerSwitchRegistry(preprocessor.getCompilerSwitchRegistry());
      delphiFile.setSourceCodeLines(readLines(sourceFile, config.getEncoding()));
      delphiFile.setTokens(createTokenList(delphiFile, preprocessor.getTokenStream()));
      delphiFile.setComments(extractComments(delphiFile.getTokens()));
    } catch (IOException | RecognitionException | EmptyDelphiFileException e) {
      throw new DelphiFileConstructionException(e);
    }
  }

  private static DelphiPreprocessor preprocess(DelphiFile delphiFile, DelphiFileConfig config)
      throws IOException {
    String filePath = delphiFile.getSourceCodeFile().getAbsolutePath();
    DelphiFileStream fileStream = new DelphiFileStream(filePath, config.getEncoding());

    DelphiLexer lexer = new DelphiLexer(fileStream, config.shouldSkipImplementation());
    DelphiPreprocessorFactory preprocessorFactory = config.getPreprocessorFactory();
    DelphiPreprocessor preprocessor = preprocessorFactory.createPreprocessor(lexer, config);
    preprocessor.process();
    return preprocessor;
  }

  private static DelphiAst createAST(
      DelphiFile delphiFile, BufferedTokenStream tokenStream, DelphiFileConfig config)
      throws RecognitionException {
    List<?> tokens = tokenStream.getTokens();
    boolean isEmptyFile =
        tokens.stream()
            .map(CommonToken.class::cast)
            .allMatch(
                token ->
                    token.getChannel() == Token.HIDDEN_CHANNEL || token.getType() == Token.EOF);

    if (isEmptyFile) {
      throw new EmptyDelphiFileException("Empty files are not allowed.");
    }

    DelphiParser parser = new DelphiParser(tokenStream);
    parser.setTreeAdaptor(new DelphiTreeAdaptor());
    DelphiNode root;

    if (config.shouldSkipImplementation()) {
      root = (DelphiNode) parser.fileWithoutImplementation().getTree();
    } else {
      root = (DelphiNode) parser.file().getTree();
    }

    return new DelphiAstImpl(delphiFile, root);
  }

  private static List<DelphiToken> createTokenList(
      DelphiFile delphiFile, DelphiTokenStream preprocessedTokenStream) throws IOException {
    String filePath = delphiFile.getSourceCodeFile().getAbsolutePath();
    DelphiLexer lexer = new DelphiLexer(new DelphiFileStream(filePath, UTF_8.name()));
    DelphiTokenStream tokenStream = new DelphiTokenStream(lexer);
    tokenStream.fill();

    List<DelphiToken> tokenList =
        tokenStream.getTokens().stream()
            .map(CommonToken.class::cast)
            .map(DelphiTokenImpl::new)
            .filter(not(DelphiToken::isEof))
            .collect(Collectors.toUnmodifiableList());

    int startIndex = 0;
    boolean include = false;

    for (Token token : preprocessedTokenStream.getTokens()) {
      if (token instanceof IncludeToken) {
        if (!include) {
          startIndex = token.getTokenIndex();
        }
        include = true;
      } else if (include) {
        offsetTokenIndices(tokenList, startIndex, token.getTokenIndex() - startIndex);
        include = false;
      }
    }

    return tokenList;
  }

  private static void offsetTokenIndices(List<DelphiToken> tokens, int startIndex, int offset) {
    tokens.stream()
        .map(DelphiTokenImpl.class::cast)
        .map(DelphiTokenImpl::getAntlrToken)
        .filter(token -> token.getTokenIndex() > startIndex)
        .forEach(token -> token.setTokenIndex(token.getTokenIndex() + offset));
  }

  private static List<DelphiToken> extractComments(List<DelphiToken> tokenList) {
    return tokenList.stream()
        .filter(DelphiToken::isComment)
        .collect(Collectors.toUnmodifiableList());
  }
}
