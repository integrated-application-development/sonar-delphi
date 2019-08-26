package org.sonar.plugins.delphi;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Preconditions;
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
import org.apache.commons.io.FileUtils;
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

public class DelphiFile {
  private InputFile inputFile;
  private File sourceCodeFile;
  private List<String> sourceCodeLines;
  private DelphiAST ast;
  private List<DelphiToken> tokens;
  private List<DelphiToken> comments;
  private Set<Integer> suppressions;

  private DelphiFile() {
    // Hide public constructor
  }

  public InputFile getInputFile() {
    return inputFile;
  }

  public File getSourceCodeFile() {
    return sourceCodeFile;
  }

  public String getSourceCodeLine(int index) {
    Preconditions.checkPositionIndex(--index, sourceCodeLines.size());
    return sourceCodeLines.get(index);
  }

  public DelphiAST getAst() {
    return ast;
  }

  public List<DelphiToken> getTokens() {
    return tokens;
  }

  public List<DelphiToken> getComments() {
    return comments;
  }

  public Set<Integer> getSuppressions() {
    return suppressions;
  }

  public static DelphiFile from(InputFile inputFile, DelphiFileStreamConfig fileStreamConfig) {
    try {
      File sourceFile = new File(DelphiUtils.uriToAbsolutePath(inputFile.uri()));
      DelphiFile delphiFile = new DelphiFile();
      delphiFile.inputFile = inputFile;
      delphiFile.sourceCodeFile = sourceFile;
      delphiFile.sourceCodeLines = FileUtils.readLines(sourceFile, fileStreamConfig.getEncoding());
      delphiFile.ast = createAST(delphiFile, fileStreamConfig);
      delphiFile.tokens = createTokenList(delphiFile);
      delphiFile.comments = extractComments(delphiFile.tokens);
      delphiFile.suppressions = findSuppressionLines(delphiFile.comments);
      return delphiFile;
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

  public static class DelphiFileConstructionException extends RuntimeException {
    private DelphiFileConstructionException(Throwable cause) {
      super("Failed to construct DelphiFile", cause);
    }
  }
}
