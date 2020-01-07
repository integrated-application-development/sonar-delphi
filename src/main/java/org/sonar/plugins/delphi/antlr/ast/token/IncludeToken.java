package org.sonar.plugins.delphi.antlr.ast.token;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.pmd.FilePosition;

public class IncludeToken extends CommonToken {
  private FilePosition insertionPosition;

  public IncludeToken(Token token, DelphiToken insertionToken) {
    super(token);
    this.insertionPosition = FilePosition.from(insertionToken);
  }

  public FilePosition getInsertionPosition() {
    return insertionPosition;
  }
}
