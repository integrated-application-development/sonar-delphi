package org.sonar.plugins.delphi.antlr.preprocessor;

import static com.google.common.base.Preconditions.checkState;
import static com.google.common.collect.Iterables.getLast;
import static org.apache.commons.io.FilenameUtils.getBaseName;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.LowercaseFileStream;
import org.sonar.plugins.delphi.antlr.preprocessor.directive.BranchDirective;
import org.sonar.plugins.delphi.antlr.preprocessor.directive.BranchingDirective;
import org.sonar.plugins.delphi.antlr.preprocessor.directive.CompilerDirective;
import org.sonar.plugins.delphi.antlr.preprocessor.directive.CompilerDirectiveType;
import org.sonar.plugins.delphi.antlr.preprocessor.directive.EndIfDirective;
import org.sonar.plugins.delphi.file.DelphiFileConfig;

public class DelphiPreprocessor {
  private static final Logger LOG = Loggers.get(DelphiPreprocessor.class);
  private final DelphiLexer lexer;
  private final DelphiFileConfig config;
  private final Set<String> definitions;
  private final List<CompilerDirective> directives;
  private final Deque<BranchingDirective> parentDirective;
  private final Map<CompilerDirectiveType, Integer> currentSwitches;
  private final CompilerSwitchRegistry switchRegistry;
  private final boolean processingIncludeFile;

  private DelphiTokenStream tokenStream;
  private Set<Token> tokens;
  private int tokenIndex;

  public DelphiPreprocessor(DelphiLexer lexer, DelphiFileConfig config) {
    this(
        lexer,
        config,
        new HashSet<>(config.getDefinitions()),
        new EnumMap<>(CompilerDirectiveType.class),
        new CompilerSwitchRegistry(),
        0,
        false);
  }

  private DelphiPreprocessor(
      DelphiLexer lexer,
      DelphiFileConfig config,
      Set<String> definitions,
      Map<CompilerDirectiveType, Integer> currentSwitches,
      CompilerSwitchRegistry switchRegistry,
      int tokenIndexStart,
      boolean processingIncludeFile) {
    this.lexer = lexer;
    this.config = config;
    this.switchRegistry = switchRegistry;
    this.definitions = definitions;
    this.directives = new ArrayList<>();
    this.parentDirective = new ArrayDeque<>();
    this.currentSwitches = currentSwitches;
    this.processingIncludeFile = processingIncludeFile;
    this.tokenIndex = tokenIndexStart;
  }

  public void process() {
    checkState(tokenStream == null, "DelphiPreprocessor.process cannot be called twice.");
    tokenStream = new DelphiTokenStream(this.lexer);

    tokenStream.fill();
    tokens = extractTokens(tokenStream);
    tokens.forEach(this::processToken);
    directives.forEach(directive -> directive.execute(this));
    tokenStream.setTokens(new ArrayList<>(tokens));
    tokenStream.reset();

    if (!processingIncludeFile) {
      registerCurrentCompilerSwitches();
    }
  }

  private static Set<Token> extractTokens(DelphiTokenStream tokenStream) {
    Set<Token> result = new TreeSet<>(Comparator.comparingInt(Token::getTokenIndex));
    result.addAll(tokenStream.getTokens());
    return result;
  }

  private void processToken(Token token) {
    token.setTokenIndex(tokenIndex++);
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
    includeFilePath = includeFilePath.replace("*", getBaseName(lexer.getSourceName()));

    Path currentPath = Path.of(lexer.getSourceName()).getParent();
    Path includeFile = Path.of(currentPath.toString(), includeFilePath);

    String includeFileName = includeFile.getFileName().toString();
    Path includePath = includeFile.getParent();

    int insertionIndex = insertionToken.getTokenIndex();
    List<Token> includeTokens = processIncludeFile(includeFileName, includePath, insertionIndex);

    this.offsetTokenIndex(insertionIndex, includeTokens.size());
    this.deleteToken(insertionToken);
    this.tokens.addAll(includeTokens);
  }

  private void offsetTokenIndex(int startIndex, int offset) {
    tokens.stream()
        .filter(token -> token.getTokenIndex() > startIndex)
        .forEach(token -> token.setTokenIndex(token.getTokenIndex() + offset));
  }

  private List<Token> processIncludeFile(String filename, Path includePath, int insertionIndex) {
    try {
      File includeFile = findIncludeFile(filename, includePath);
      if (includeFile == null) {
        includeFile = findIncludeFileInSearchPath(filename);
      }

      if (includeFile != null) {
        String path = includeFile.getCanonicalPath();

        if (path.equals(lexer.getSourceName())) {
          throw new RuntimeException(
              "Self-referencing include file <" + includeFile.getAbsolutePath() + ">");
        }

        LowercaseFileStream fileStream = new LowercaseFileStream(path, config.getEncoding());
        DelphiLexer includeLexer = new DelphiLexer(fileStream);
        DelphiPreprocessor preprocessor =
            new DelphiPreprocessor(
                includeLexer,
                config,
                definitions,
                currentSwitches,
                switchRegistry,
                insertionIndex,
                true);
        preprocessor.process();
        DelphiTokenStream includeTokenStream = preprocessor.getTokenStream();
        List<Token> result = includeTokenStream.getTokens();
        // Remove EOF
        result.remove(result.size() - 1);
        return result;
      }
    } catch (IOException | RuntimeException e) {
      LOG.debug("Error occurred while resolving includes: ", e);
    }

    LOG.warn("Failed to resolve include '" + filename + "'.");
    return Collections.emptyList();
  }

  @Nullable
  private File findIncludeFile(String includeFileName, Path path) throws IOException {
    Set<Path> directories = new HashSet<>();

    try (DirectoryStream<Path> stream = Files.newDirectoryStream(path)) {
      for (Path item : stream) {
        if (Files.isDirectory(item)) {
          directories.add(item);
        } else if (includeFileName.equalsIgnoreCase(item.getFileName().toString())) {
          return item.toFile();
        }
      }
    }

    for (Path directory : directories) {
      File result = findIncludeFile(includeFileName, directory);
      if (result != null) {
        return result;
      }
    }

    return null;
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

  public void handleSwitch(CompilerDirectiveType type, int tokenIndex, boolean value) {
    if (value) {
      currentSwitches.put(type, tokenIndex);
      return;
    }

    Integer startIndex = currentSwitches.remove(type);
    if (startIndex != null) {
      switchRegistry.addSwitch(type, startIndex, tokenIndex);
    }
  }

  private void registerCurrentCompilerSwitches() {
    if (!tokens.isEmpty()) {
      int lastTokenIndex = getLast(tokens).getTokenIndex();
      currentSwitches.forEach((type, index) -> handleSwitch(type, lastTokenIndex, false));
    }
  }

  public DelphiTokenStream getTokenStream() {
    return tokenStream;
  }

  public CompilerSwitchRegistry getCompilerSwitchRegistry() {
    return switchRegistry;
  }
}
