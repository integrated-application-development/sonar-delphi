package org.sonar.plugins.delphi.symbol;

import java.util.HashMap;
import java.util.Map;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.DelphiToken;

public enum MethodDirective {
  OVERLOAD(DelphiLexer.OVERLOAD),
  REINTRODUCE(DelphiLexer.REINTRODUCE),
  MESSAGE(DelphiLexer.MESSAGE),
  STATIC(DelphiLexer.STATIC),
  DYNAMIC(DelphiLexer.DYNAMIC),
  OVERRIDE(DelphiLexer.OVERRIDE),
  VIRTUAL(DelphiLexer.VIRTUAL),
  ABSTRACT(DelphiLexer.ABSTRACT),
  FINAL(DelphiLexer.FINAL),
  INLINE(DelphiLexer.INLINE),
  ASSEMBLER(DelphiLexer.ASSEMBLER),
  CDECL(DelphiLexer.CDECL),
  PASCAL(DelphiLexer.PASCAL),
  REGISTER(DelphiLexer.REGISTER),
  SAFECALL(DelphiLexer.SAFECALL),
  STDCALL(DelphiLexer.STDCALL),
  EXPORT(DelphiLexer.EXPORT),
  FAR(DelphiLexer.FAR),
  LOCAL(DelphiLexer.LOCAL),
  NEAR(DelphiLexer.NEAR),
  DEPRECATED(DelphiLexer.DEPRECATED),
  EXPERIMENTAL(DelphiLexer.EXPERIMENTAL),
  PLATFORM(DelphiLexer.PLATFORM),
  LIBRARY(DelphiLexer.LIBRARY),
  VARARGS(DelphiLexer.VARARGS),
  EXTERNAL(DelphiLexer.EXTERNAL),
  NAME(DelphiLexer.NAME),
  INDEX(DelphiLexer.INDEX),
  DISPID(DelphiLexer.DISPID);

  private final int tokenType;
  private static final Map<Integer, MethodDirective> tokenTypeMap = new HashMap<>();

  static {
    for (MethodDirective directive : MethodDirective.values()) {
      tokenTypeMap.put(directive.tokenType, directive);
    }
  }

  MethodDirective(int tokenType) {
    this.tokenType = tokenType;
  }

  public static MethodDirective fromToken(DelphiToken token) {
    return tokenTypeMap.get(token.getType());
  }
}
