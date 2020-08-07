package org.sonar.plugins.delphi.preprocessor.directive;

import org.antlr.runtime.Token;

public class HintsDirective extends SwitchDirective {
  public HintsDirective(Token token, CompilerDirectiveType type, String value) {
    super(token, type, value.equalsIgnoreCase("ON"));
  }
}
