/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
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
package au.com.integradev.delphi.preprocessor;

import static java.util.Comparator.comparingInt;

import au.com.integradev.delphi.antlr.DelphiFileStream;
import au.com.integradev.delphi.antlr.DelphiLexer;
import au.com.integradev.delphi.antlr.DelphiTokenStream;
import au.com.integradev.delphi.antlr.ast.token.DelphiTokenImpl;
import au.com.integradev.delphi.antlr.ast.token.IncludeToken;
import au.com.integradev.delphi.compiler.Platform;
import au.com.integradev.delphi.file.DelphiFileConfig;
import au.com.integradev.delphi.preprocessor.directive.BranchDirective;
import au.com.integradev.delphi.preprocessor.directive.BranchingDirective;
import au.com.integradev.delphi.preprocessor.directive.CompilerDirectiveImpl;
import au.com.integradev.delphi.preprocessor.directive.CompilerDirectiveParserImpl;
import au.com.integradev.delphi.utils.DelphiUtils;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.apache.commons.io.FilenameUtils;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.communitydelphi.api.directive.CompilerDirective;
import org.sonar.plugins.communitydelphi.api.directive.CompilerDirectiveParser;
import org.sonar.plugins.communitydelphi.api.directive.ConditionalDirective;
import org.sonar.plugins.communitydelphi.api.directive.SwitchDirective.SwitchKind;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public class DelphiPreprocessor {
  private static final Logger LOG = Loggers.get(DelphiPreprocessor.class);
  private final DelphiLexer lexer;
  private final DelphiFileConfig config;
  private final Platform platform;
  private final Set<String> definitions;
  private final List<CompilerDirective> directives;
  private final Deque<BranchingDirective> parentDirective;
  private final Map<SwitchKind, Integer> currentSwitches;
  private final CompilerSwitchRegistry switchRegistry;
  private final boolean processingIncludeFile;

  private DelphiTokenStream tokenStream;
  private Set<Token> tokens;
  private int tokenIndex;

  DelphiPreprocessor(DelphiLexer lexer, DelphiFileConfig config, Platform platform) {
    this(
        lexer,
        config,
        platform,
        caseInsensitiveSet(config.getDefinitions()),
        new EnumMap<>(SwitchKind.class),
        new CompilerSwitchRegistry(),
        0,
        false);
  }

  private DelphiPreprocessor(
      DelphiLexer lexer,
      DelphiFileConfig config,
      Platform platform,
      Set<String> definitions,
      Map<SwitchKind, Integer> currentSwitches,
      CompilerSwitchRegistry switchRegistry,
      int tokenIndexStart,
      boolean processingIncludeFile) {
    this.lexer = lexer;
    this.config = config;
    this.platform = platform;
    this.switchRegistry = switchRegistry;
    this.definitions = definitions;
    this.directives = new ArrayList<>();
    this.parentDirective = new ArrayDeque<>();
    this.currentSwitches = currentSwitches;
    this.processingIncludeFile = processingIncludeFile;
    this.tokenIndex = tokenIndexStart;
  }

  private static Set<String> caseInsensitiveSet(Set<String> set) {
    Set<String> result = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    result.addAll(set);
    return result;
  }

  public void process() {
    Preconditions.checkState(
        tokenStream == null, "DelphiPreprocessor.process cannot be called twice.");
    tokenStream = new DelphiTokenStream(lexer);

    tokenStream.fill();
    tokens = extractTokens(tokenStream);
    tokens.forEach(this::processToken);
    directives.stream()
        .map(CompilerDirectiveImpl.class::cast)
        .forEach(directive -> directive.execute(this));
    tokenStream.setTokens(new ArrayList<>(tokens));
    tokenStream.reset();

    if (!processingIncludeFile) {
      registerCurrentCompilerSwitches();
    }
  }

  private static Set<Token> extractTokens(DelphiTokenStream tokenStream) {
    Set<Token> result = new TreeSet<>(comparingInt(Token::getTokenIndex));
    result.addAll(tokenStream.getTokens());
    return result;
  }

  private void processToken(Token token) {
    token.setTokenIndex(tokenIndex);
    tokenIndex++;

    if (token.getType() == DelphiLexer.TkCompilerDirective) {
      CompilerDirectiveParser parser = new CompilerDirectiveParserImpl(platform);
      DelphiToken directiveToken = new DelphiTokenImpl(token);
      parser.parse(directiveToken).ifPresent(this::processDirective);
    } else if (!parentDirective.isEmpty()) {
      parentDirective.peek().addToken(token);
    }
  }

  private void processDirective(CompilerDirective directive) {
    if (directive instanceof ConditionalDirective) {
      switch (((ConditionalDirective) directive).kind()) {
        case IF:
        case IFDEF:
        case IFNDEF:
        case IFOPT:
          addBranchingDirective(new BranchingDirective((BranchDirective) directive));
          break;
        case ELSEIF:
        case ELSE:
          addBranch((BranchDirective) directive);
          break;
        case IFEND:
        case ENDIF:
          endBranchingDirective();
          break;
      }
    } else if (parentDirective.isEmpty()) {
      directives.add(directive);
    } else {
      parentDirective.peek().addDirective(directive);
    }
  }

  public void deleteToken(Token token) {
    tokens.remove(token);
  }

  public void resolveInclude(Token insertionToken, String includeFilePath) {
    includeFilePath =
        DelphiUtils.normalizeFileName(
            includeFilePath.replace("*", FilenameUtils.getBaseName(lexer.getSourceName())));

    String currentParentPath = Path.of(lexer.getSourceName()).getParent().toString();
    Path includeFile = Path.of(includeFilePath);
    if (!includeFile.isAbsolute()) {
      includeFile = Path.of(currentParentPath, includeFilePath);
    }
    includeFile = includeFile.normalize();

    String includeFileName = includeFile.getFileName().toString();
    Path includePath = includeFile.getParent();

    DelphiToken location = new DelphiTokenImpl(insertionToken);
    List<Token> includeTokens = processIncludeFile(includeFileName, includePath, location);

    offsetTokenIndices(location.getIndex(), getTokenOffset(includeTokens));
    deleteToken(insertionToken);
    tokens.addAll(includeTokens);
  }

  private static int getTokenOffset(List<Token> tokens) {
    if (!tokens.isEmpty()) {
      return Iterables.getLast(tokens).getTokenIndex() - tokens.get(0).getTokenIndex();
    }
    return 0;
  }

  private void offsetTokenIndices(int startIndex, int offset) {
    tokens.stream()
        .filter(token -> token.getTokenIndex() > startIndex)
        .forEach(token -> token.setTokenIndex(token.getTokenIndex() + offset));
  }

  private List<Token> processIncludeFile(String filename, Path includePath, DelphiToken location) {
    try {
      Path includeFile = config.getSearchPath().search(filename, includePath);

      if (includeFile != null) {
        String path = includeFile.toAbsolutePath().normalize().toString();

        if (path.equals(lexer.getSourceName())) {
          throw new RuntimeException(
              "Self-referencing include file <" + includeFile.toAbsolutePath() + ">");
        }

        DelphiFileStream fileStream = new DelphiFileStream(path, config.getEncoding());
        DelphiLexer includeLexer = new DelphiLexer(fileStream);
        DelphiPreprocessor preprocessor =
            new DelphiPreprocessor(
                includeLexer,
                config,
                platform,
                definitions,
                currentSwitches,
                switchRegistry,
                location.getIndex(),
                true);

        preprocessor.process();

        List<Token> includeTokens = preprocessor.getTokenStream().getTokens();
        return includeTokens.stream()
            .limit(includeTokens.size() - 1L)
            .map(token -> new IncludeToken(token, location))
            .collect(Collectors.toList());
      }
    } catch (IOException | RuntimeException e) {
      LOG.debug("Error occurred while resolving includes: ", e);
    }

    LOG.warn("Failed to resolve include '" + filename + "'.");
    return Collections.emptyList();
  }

  private void addBranchingDirective(BranchingDirective directive) {
    if (!parentDirective.isEmpty()) {
      parentDirective.peek().addDirective(directive);
    }
    parentDirective.push(directive);
  }

  private void addBranch(BranchDirective directive) {
    Preconditions.checkState(!parentDirective.isEmpty());
    parentDirective.peek().addBranch(directive);
  }

  private void endBranchingDirective() {
    Preconditions.checkState(!parentDirective.isEmpty());
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

  public void handleSwitch(SwitchKind kind, int tokenIndex, boolean value) {
    if (value) {
      currentSwitches.put(kind, tokenIndex);
      return;
    }

    Integer startIndex = currentSwitches.remove(kind);
    if (startIndex != null) {
      switchRegistry.addSwitch(kind, startIndex, tokenIndex);
    }
  }

  private void registerCurrentCompilerSwitches() {
    if (!tokens.isEmpty()) {
      int lastTokenIndex = Iterables.getLast(tokens).getTokenIndex();
      currentSwitches.forEach((type, index) -> handleSwitch(type, lastTokenIndex, false));
    }
  }

  public DelphiTokenStream getTokenStream() {
    return tokenStream;
  }

  public CompilerSwitchRegistry getCompilerSwitchRegistry() {
    return switchRegistry;
  }

  public TypeFactory getTypeFactory() {
    return config.getTypeFactory();
  }
}
