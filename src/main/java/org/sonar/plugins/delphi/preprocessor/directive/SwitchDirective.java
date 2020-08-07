package org.sonar.plugins.delphi.preprocessor.directive;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.preprocessor.DelphiPreprocessor;

public class SwitchDirective extends AbstractCompilerDirective {
  private final boolean value;

  SwitchDirective(Token token, CompilerDirectiveType type, boolean value) {
    super(token, type);
    this.value = value;
  }

  @Override
  public void execute(DelphiPreprocessor preprocessor) {
    preprocessor.handleSwitch(getType(), getToken().getTokenIndex(), value);
  }

  public boolean isOn() {
    return value;
  }

  public boolean isOff() {
    return !value;
  }
}
