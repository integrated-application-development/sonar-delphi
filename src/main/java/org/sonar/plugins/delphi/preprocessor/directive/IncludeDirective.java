package org.sonar.plugins.delphi.preprocessor.directive;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.preprocessor.DelphiPreprocessor;

class IncludeDirective extends DefaultCompilerDirective {
  private final String includeFile;

  IncludeDirective(Token token, CompilerDirectiveType type, String includeFile) {
    super(token, type);
    this.includeFile = includeFile;
  }

  @Override
  public void execute(DelphiPreprocessor preprocessor) {
    preprocessor.resolveInclude(getToken(), includeFile);
  }
}
