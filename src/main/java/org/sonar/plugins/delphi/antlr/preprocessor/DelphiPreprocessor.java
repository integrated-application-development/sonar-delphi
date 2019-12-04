package org.sonar.plugins.delphi.antlr.preprocessor;

import static com.google.common.base.Preconditions.checkState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenRewriteStream;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.LowercaseFileStream;
import org.sonar.plugins.delphi.antlr.preprocessor.directive.BranchDirective;
import org.sonar.plugins.delphi.antlr.preprocessor.directive.BranchingDirective;
import org.sonar.plugins.delphi.antlr.preprocessor.directive.CompilerDirective;
import org.sonar.plugins.delphi.antlr.preprocessor.directive.EndIfDirective;
import org.sonar.plugins.delphi.file.DelphiFileConfig;

public class DelphiPreprocessor {
  private static final Logger LOG = Loggers.get(DelphiPreprocessor.class);
  private final DelphiLexer lexer;
  private final DelphiFileConfig config;
  private final Set<String> definitions;
  private final List<CompilerDirective> directives;
  private final Deque<BranchingDirective> parentDirective;

  private List<Token> tokens;

  public DelphiPreprocessor(DelphiLexer lexer, DelphiFileConfig config) {
    this.lexer = lexer;
    this.config = config;
    this.definitions = new HashSet<>(config.getDefinitions());
    this.directives = new ArrayList<>();
    this.parentDirective = new ArrayDeque<>();
  }

  public TokenRewriteStream process() {
    TokenRewriteStream tokenStream = new TokenRewriteStream(this.lexer);

    tokenStream.fill();
    tokens = extractTokens(tokenStream);
    tokens.forEach(this::processToken);
    directives.forEach(directive -> directive.execute(this));
    tokenStream.reset();

    return tokenStream;
  }

  @SuppressWarnings("unchecked")
  private static List<Token> extractTokens(TokenRewriteStream tokenStream) {
    // NOTE: antlr likes to return raw lists, even though we know the list contains Token objects.
    // Trying to return a new list instead of casting will break the preprocessor.
    return (List<Token>) tokenStream.getTokens();
  }

  private void processToken(Token token) {
    if (token.getType() == DelphiLexer.TkCompilerDirective) {
      CompilerDirective directive = CompilerDirective.fromToken(token);
      processDirective(directive);
    } else if (!parentDirective.isEmpty()) {
      parentDirective.peek().addToken(token);
    }
  }

  private void processDirective(CompilerDirective directive) {
    if (directive instanceof BranchingDirective) {
      addBranchingDirective((BranchingDirective) directive);
    } else if (directive instanceof BranchDirective) {
      addBranch((BranchDirective) directive);
    } else if (directive instanceof EndIfDirective) {
      endBranchingDirective();
    } else if (parentDirective.isEmpty()) {
      directives.add(directive);
    } else {
      parentDirective.peek().addDirective(directive);
    }
  }

  public void deleteToken(Token token) {
    this.tokens.remove(token);
  }

  public void resolveInclude(Token insertionToken, String includeFilePath) {
    Path currentPath = Path.of(lexer.getSourceName()).getParent();
    Path includeFile = Path.of(currentPath.toString(), includeFilePath);

    String includeFileName = includeFile.getFileName().toString();
    Path includePath = includeFile.getParent();

    List<Token> includeTokens = processIncludeFile(includeFileName, includePath);
    int insertIndex = this.tokens.indexOf(insertionToken);

    this.tokens.addAll(insertIndex, includeTokens);
    this.deleteToken(insertionToken);
  }

  private List<Token> processIncludeFile(String includeFileName, Path includePath) {
    try {
      File includeFile = findIncludeFile(includeFileName, includePath);
      if (includeFile == null) {
        includeFile = findIncludeFileInSearchPath(includeFileName);
      }

      if (includeFile != null) {
        String path = includeFile.getCanonicalPath();
        LowercaseFileStream fileStream = new LowercaseFileStream(path, config.getEncoding());
        DelphiLexer includeLexer = new DelphiLexer(fileStream);
        DelphiPreprocessor preprocessor = new DelphiPreprocessor(includeLexer, config);
        TokenRewriteStream tokenStream = preprocessor.process();
        List<Token> result = extractTokens(tokenStream);
        // Remove EOF
        result.remove(result.size() - 1);
        return result;
      }
    } catch (IOException | RuntimeException e) {
      LOG.debug("Error occurred while resolving includes: ", e);
    }

    LOG.warn("Failed to resolve include '" + includeFileName + "'.");
    return Collections.emptyList();
  }

  @Nullable
  private File findIncludeFile(String includeFileName, Path path) throws IOException {
    try (var stream =
        Files.find(path, Integer.MAX_VALUE, (filePath, attributes) -> attributes.isRegularFile())) {
      return stream
          .filter(file -> file.getFileName().toString().equalsIgnoreCase(includeFileName))
          .map(Path::toFile)
          .findFirst()
          .orElse(null);
    }
  }

  @Nullable
  private File findIncludeFileInSearchPath(String includeFileName) throws IOException {
    File includeFile = null;
    for (Path path : config.getSearchPath()) {
      includeFile = findIncludeFile(includeFileName, path);
      if (includeFile != null) {
        break;
      }
    }
    return includeFile;
  }

  private void addBranchingDirective(BranchingDirective directive) {
    if (!parentDirective.isEmpty()) {
      parentDirective.peek().addDirective(directive);
    }
    parentDirective.push(directive);
  }

  private void addBranch(BranchDirective directive) {
    checkState(!parentDirective.isEmpty());
    parentDirective.peek().addBranch(directive);
  }

  private void endBranchingDirective() {
    checkState(!parentDirective.isEmpty());
    BranchingDirective directive = parentDirective.pop();
    if (parentDirective.isEmpty()) {
      directives.add(directive);
    }
  }

  public boolean isDefined(String define) {
    return definitions.contains(define);
  }

  public void define(String define) {
    definitions.add(define);
  }

  public void undefine(String define) {
    definitions.remove(define);
  }
}
