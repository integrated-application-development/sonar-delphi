package org.sonar.plugins.delphi.preprocessor.directive;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.preprocessor.DelphiPreprocessor;

public class DefineDirective extends AbstractCompilerDirective {
  private final String define;

  DefineDirective(Token token, CompilerDirectiveType type, String define) {
    super(token, type);
    this.define = define;
  }

  @Override
  public void execute(DelphiPreprocessor preprocessor) {
    preprocessor.define(define);
  }
}
