package org.sonar.plugins.delphi.antlr.preprocessor.directive;

import org.antlr.runtime.Token;

public abstract class AbstractCompilerDirective implements CompilerDirective {
  private Token token;
  private CompilerDirectiveType type;

  public AbstractCompilerDirective(Token token, CompilerDirectiveType type) {
    this.token = token;
    this.type = type;
  }

  @Override
  public Token getToken() {
    return token;
  }

  @Override
  public CompilerDirectiveType getType() {
    return type;
  }
}
