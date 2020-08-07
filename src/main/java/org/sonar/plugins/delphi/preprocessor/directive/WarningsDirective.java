package org.sonar.plugins.delphi.preprocessor.directive;

import org.antlr.runtime.Token;

public class WarningsDirective extends SwitchDirective {
  public WarningsDirective(Token token, CompilerDirectiveType type, String value) {
    super(token, type, value.equalsIgnoreCase("ON"));
  }
}
