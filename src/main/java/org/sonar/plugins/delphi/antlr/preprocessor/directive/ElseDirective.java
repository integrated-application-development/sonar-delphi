package org.sonar.plugins.delphi.antlr.preprocessor.directive;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.preprocessor.DelphiPreprocessor;

public class ElseDirective extends BranchDirective {
  ElseDirective(Token token, CompilerDirectiveType type) {
    super(token, type);
  }

  @Override
  boolean isSuccessfulBranch(DelphiPreprocessor preprocessor) {
    return true;
  }
}
