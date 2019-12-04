package org.sonar.plugins.delphi.antlr.preprocessor.directive;

import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.preprocessor.DelphiPreprocessor;

public abstract class BranchDirective extends AbstractCompilerDirective {
  private List<CompilerDirective> directives;
  private List<Token> tokens;

  BranchDirective(Token token, CompilerDirectiveType type) {
    super(token, type);
    this.directives = new ArrayList<>();
    this.tokens = new ArrayList<>();
  }

  List<CompilerDirective> getDirectives() {
    return directives;
  }

  List<Token> getTokens() {
    return tokens;
  }

  void addDirective(CompilerDirective directive) {
    directives.add(directive);
  }

  void addToken(Token token) {
    tokens.add(token);
  }

  @Override
  public void execute(DelphiPreprocessor preprocessor) {
    directives.forEach(directive -> directive.execute(preprocessor));
  }

  abstract boolean isSuccessfulBranch(DelphiPreprocessor preprocessor);
}
