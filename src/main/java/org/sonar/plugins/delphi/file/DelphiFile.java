package org.sonar.plugins.delphi.file;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Predicate.not;
import static org.antlr.runtime.Token.EOF;
import static org.antlr.runtime.Token.HIDDEN_CHANNEL;
import static org.apache.commons.io.FileUtils.readLines;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.sourceforge.pmd.lang.ast.GenericToken;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenRewriteStream;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.DelphiParser;
import org.sonar.plugins.delphi.antlr.LowercaseFileStream;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.DelphiToken;
import org.sonar.plugins.delphi.antlr.ast.DelphiTreeAdaptor;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.preprocessor.DelphiPreprocessor;
import org.sonar.plugins.delphi.pmd.DelphiPmdConstants;
import org.sonar.plugins.delphi.utils.DelphiUtils;

public interface DelphiFile {
  File getSourceCodeFile();

  String getSourceCodeLine(int index);

  DelphiAST getAst();

  List<DelphiToken> getTokens();

  List<DelphiToken> getComments();

  Set<Integer> getSuppressions();

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
      super("Failed to construct DelphiFile", cause);
    }
  }

  static DelphiFileConfig createConfig(String encoding) {
    return new DefaultDelphiFileConfig(encoding, Collections.emptyList(), Collections.emptySet());
  }

  static DelphiFileConfig createConfig(
      String encoding, List<Path> searchPath, Set<String> definitions) {
    return new DefaultDelphiFileConfig(encoding, searchPath, definitions);
  }

  static DelphiFile from(File sourceFile, DelphiFileConfig config) {
    DefaultDelphiFile delphiFile = new DefaultDelphiFile();
    setupFile(delphiFile, sourceFile, config);
    return delphiFile;
  }

  static void setupFile(DefaultDelphiFile delphiFile, File sourceFile, DelphiFileConfig config) {
    try {
      delphiFile.setSourceCodeFile(sourceFile);
      delphiFile.setSourceCodeLines(readLines(sourceFile, config.getEncoding()));
      delphiFile.setAst(createAST(delphiFile, config));
      delphiFile.setTokens(createTokenList(delphiFile));
      delphiFile.setComments(extractComments(delphiFile.getTokens()));
      delphiFile.setSuppressions(findSuppressionLines(delphiFile.getComments()));
    } catch (IOException | RecognitionException | RuntimeException e) {
      throw new DelphiFileConstructionException(e);
    }
  }

  private static DelphiAST createAST(DelphiFile delphiFile, DelphiFileConfig config)
      throws IOException, RecognitionException {
    String filePath = delphiFile.getSourceCodeFile().getAbsolutePath();
    LowercaseFileStream fileStream = new LowercaseFileStream(filePath, config.getEncoding());

    DelphiLexer lexer = new DelphiLexer(fileStream);
    DelphiPreprocessor preprocessor = new DelphiPreprocessor(lexer, config);
    TokenRewriteStream tokenStream = preprocessor.process();

    List<?> tokens = tokenStream.getTokens();
    boolean isEmptyFile =
        tokens.stream()
            .map(CommonToken.class::cast)
            .allMatch(token -> token.getChannel() == HIDDEN_CHANNEL || token.getType() == EOF);

    if (isEmptyFile) {
      throw new RuntimeException("Empty files are not allowed.");
    }

    DelphiParser parser = new DelphiParser(tokenStream);
    parser.setTreeAdaptor(new DelphiTreeAdaptor());
    DelphiNode root = (DelphiNode) parser.file().getTree();

    return new DelphiAST(delphiFile, root);
  }

  private static List<DelphiToken> createTokenList(DelphiFile delphiFile) throws IOException {
    String filePath = delphiFile.getSourceCodeFile().getAbsolutePath();
    DelphiLexer lexer = new DelphiLexer(new LowercaseFileStream(filePath, UTF_8.name()));
    CommonTokenStream tokenStream = new CommonTokenStream(lexer);
    tokenStream.fill();

    List<?> tokenObjects = tokenStream.getTokens();
    return tokenObjects.stream()
        .map(CommonToken.class::cast)
        .map(DelphiToken::new)
        .filter(not(DelphiToken::isEof))
        .collect(Collectors.toUnmodifiableList());
  }

  private static List<DelphiToken> extractComments(List<DelphiToken> tokenList) {
    return tokenList.stream()
        .filter(DelphiToken::isComment)
        .collect(Collectors.toUnmodifiableList());
  }

  private static Set<Integer> findSuppressionLines(List<DelphiToken> commentList) {
    return commentList.stream()
        .filter(comment -> comment.getImage().contains(DelphiPmdConstants.SUPPRESSION_TAG))
        .map(GenericToken::getBeginLine)
        .collect(Collectors.toUnmodifiableSet());
  }
}
