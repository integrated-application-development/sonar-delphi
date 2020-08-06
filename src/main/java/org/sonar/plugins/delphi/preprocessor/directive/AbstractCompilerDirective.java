package org.sonar.plugins.delphi.preprocessor.directive;

import org.antlr.runtime.Token;

public abstract class AbstractCompilerDirective implements CompilerDirective {
  private final Token token;
  private final CompilerDirectiveType type;

  protected AbstractCompilerDirective(Token token, CompilerDirectiveType type) {
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
