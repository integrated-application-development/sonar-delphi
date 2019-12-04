package org.sonar.plugins.delphi.antlr.preprocessor.directive;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.preprocessor.DelphiPreprocessor;

public class EndIfDirective extends AbstractCompilerDirective {
  EndIfDirective(Token token, CompilerDirectiveType type) {
    super(token, type);
  }

  @Override
  public void execute(DelphiPreprocessor preprocessor) {
    // The preprocessor uses this directive as a flag to pop a branching directive off the stack.
    // It doesn't actually do anything at execution time.
  }
}
