package org.sonar.plugins.delphi.antlr.preprocessor.directive;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.preprocessor.DelphiPreprocessor;

class IfDefDirective extends BranchDirective {
  private final String define;
  private final boolean isPositive;

  IfDefDirective(Token token, CompilerDirectiveType type, String define, boolean isPositive) {
    super(token, type);
    this.define = define;
    this.isPositive = isPositive;
  }

  @Override
  boolean isSuccessfulBranch(DelphiPreprocessor preprocessor) {
    return preprocessor.isDefined(define) == isPositive;
  }
}
