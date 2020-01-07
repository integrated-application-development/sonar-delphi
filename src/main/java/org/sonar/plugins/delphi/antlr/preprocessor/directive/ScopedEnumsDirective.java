package org.sonar.plugins.delphi.antlr.preprocessor.directive;

import org.antlr.runtime.Token;

public class ScopedEnumsDirective extends SwitchDirective {
  ScopedEnumsDirective(Token token, CompilerDirectiveType type, String value) {
    super(token, type, value.equalsIgnoreCase("ON"));
  }
}
