package org.sonar.plugins.delphi.antlr.preprocessor.directive;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.preprocessor.DelphiPreprocessor;

public class UndefineDirective extends DefaultCompilerDirective {
  private final String define;

  UndefineDirective(Token token, CompilerDirectiveType type, String define) {
    super(token, type);
    this.define = define;
  }

  @Override
  public void execute(DelphiPreprocessor preprocessor) {
    preprocessor.undefine(define);
  }
}
