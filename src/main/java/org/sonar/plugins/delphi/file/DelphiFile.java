package org.sonar.plugins.delphi.file;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.commons.io.FileUtils.readLines;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import net.sourceforge.pmd.lang.ast.GenericToken;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.TokenRewriteStream;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.DelphiParser;
import org.sonar.plugins.delphi.antlr.ast.DelphiAST;
import org.sonar.plugins.delphi.antlr.ast.DelphiToken;
import org.sonar.plugins.delphi.antlr.ast.DelphiTreeAdaptor;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStream;
import org.sonar.plugins.delphi.antlr.filestream.DelphiFileStreamConfig;
import org.sonar.plugins.delphi.antlr.filestream.LowercaseFileStream;
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

    static DelphiInputFile from(InputFile inputFile, DelphiFileStreamConfig fileStreamConfig) {
      DefaultDelphiInputFile delphiFile = new DefaultDelphiInputFile();
      File sourceFile = new File(DelphiUtils.uriToAbsolutePath(inputFile.uri()));
      setupFile(delphiFile, sourceFile, fileStreamConfig);
      delphiFile.setInputFile(inputFile);
      return delphiFile;
    }
  }

  class DelphiFileConstructionException extends RuntimeException {
    DelphiFileConstructionException(Throwable cause) {
      super("Failed to construct DelphiFile", cause);
    }
  }

  static DelphiFile from(File sourceFile, DelphiFileStreamConfig fileStreamConfig) {
    DefaultDelphiFile delphiFile = new DefaultDelphiFile();
    setupFile(delphiFile, sourceFile, fileStreamConfig);
    return delphiFile;
  }

  static void setupFile(
      DefaultDelphiFile delphiFile, File sourceFile, DelphiFileStreamConfig fileStreamConfig) {
    try {
      delphiFile.setSourceCodeFile(sourceFile);
      delphiFile.setSourceCodeLines(readLines(sourceFile, fileStreamConfig.getEncoding()));
      delphiFile.setAst(createAST(delphiFile, fileStreamConfig));
      delphiFile.setTokens(createTokenList(delphiFile));
      delphiFile.setComments(extractComments(delphiFile.getTokens()));
      delphiFile.setSuppressions(findSuppressionLines(delphiFile.getComments()));
    } catch (IOException | RecognitionException | RuntimeException e) {
      throw new DelphiFileConstructionException(e);
    }
  }

  private static DelphiAST createAST(DelphiFile delphiFile, DelphiFileStreamConfig config)
      throws IOException, RecognitionException {
    String filePath = delphiFile.getSourceCodeFile().getAbsolutePath();
    DelphiFileStream fileStream = new DelphiFileStream(filePath, config);
    DelphiLexer lexer = new DelphiLexer(fileStream);
    TokenRewriteStream tokenStream = new TokenRewriteStream(lexer);

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
        .filter(Predicate.not(DelphiToken::isEof))
        .collect(Collectors.toUnmodifiableList());
  }

  private static List<DelphiToken> extractComments(List<DelphiToken> tokenList) {
    return tokenList.stream().filter(DelphiToken::isComment).collect(Collectors.toList());
  }

  private static Set<Integer> findSuppressionLines(List<DelphiToken> commentList) {
    return commentList.stream()
        .filter(comment -> comment.getImage().contains(DelphiPmdConstants.SUPPRESSION_TAG))
        .map(GenericToken::getBeginLine)
        .collect(Collectors.toSet());
  }
}
