package org.sonar.plugins.delphi.preprocessor.directive;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.preprocessor.DelphiPreprocessor;

class DefaultCompilerDirective extends AbstractCompilerDirective {

  public DefaultCompilerDirective(Token token, CompilerDirectiveType type) {
    super(token, type);
  }

  @Override
  public void execute(DelphiPreprocessor preprocessor) {
    // Do nothing
  }
}
