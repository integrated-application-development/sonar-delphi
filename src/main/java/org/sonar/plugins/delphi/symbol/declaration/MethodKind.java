package org.sonar.plugins.delphi.symbol.declaration;

import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.DelphiParser;

public enum MethodKind {
  CONSTRUCTOR(DelphiLexer.CONSTRUCTOR),
  DESTRUCTOR(DelphiLexer.DESTRUCTOR),
  FUNCTION(DelphiLexer.FUNCTION),
  OPERATOR(DelphiLexer.OPERATOR),
  PROCEDURE(DelphiLexer.PROCEDURE);

  private final int tokenType;

  MethodKind(int tokenType) {
    this.tokenType = tokenType;
  }

  public static MethodKind fromTokenType(int tokenType) {
    for (MethodKind kind : MethodKind.values()) {
      if (kind.tokenType == tokenType) {
        return kind;
      }
    }

    throw new AssertionError(
        "Unhandled MethodKind token type: " + DelphiParser.tokenNames[tokenType]);
  }
}
