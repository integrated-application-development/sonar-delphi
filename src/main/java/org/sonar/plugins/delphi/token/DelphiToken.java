package org.sonar.plugins.delphi.token;

import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;
import org.sonar.plugins.delphi.core.DelphiKeywords;

class DelphiToken {
  public static final String STRING_LITERAL = "STRING_LITERAL";
  public static final String NUMERIC_LITERAL = "NUMERIC_LITERAL";

  private Token token;
  private int startLine;
  private int startColumn;
  private int endLine;
  private int endColumn;

  DelphiToken(Token token, int startLine, int startColumn, int endLine, int endColumn) {
    this.token = token;
    this.startLine = startLine;
    this.startColumn = startColumn;
    this.endLine = endLine;
    this.endColumn = endColumn;
  }

  Token getToken() {
    return token;
  }

  int getStartLine() {
    return startLine;
  }

  int getStartColumn() {
    return startColumn;
  }

  int getEndLine() {
    return endLine;
  }

  int getEndColumn() {
    return endColumn;
  }

  String getImage() {
    if (isStringLiteral()) {
      return STRING_LITERAL;
    }

    if (isNumericLiteral()) {
      return NUMERIC_LITERAL;
    }

    return token.getText().toLowerCase();
  }

  private boolean isStringLiteral() {
    return token.getType() == DelphiLexer.QuotedString;
  }

  private boolean isNumericLiteral() {
    return token.getType() == DelphiLexer.TkIntNum
        || token.getType() == DelphiLexer.TkRealNum
        || token.getType() == DelphiLexer.TkHexNum;
  }

  boolean isWhitespace() {
    return token.getType() == DelphiLexer.WS;
  }

  boolean isComment() {
    return token.getType() == DelphiLexer.COMMENT;
  }

  private boolean isKeyword() {
    return DelphiKeywords.KEYWORDS.contains(token.getType());
  }

  private boolean isSpecialKeyword() {
    return DelphiKeywords.SPECIAL_KEYWORDS.contains(token.getType());
  }

  @Nullable
  public TypeOfText getHighlightingType() {
    TypeOfText type = null;

    if (isStringLiteral()) {
      type = TypeOfText.STRING;
    } else if (isNumericLiteral()) {
      type = TypeOfText.CONSTANT;
    } else if (isComment()) {
      type = TypeOfText.COMMENT;
    } else if (isKeyword()) {
      type = TypeOfText.KEYWORD;
    } else if (isSpecialKeyword()) {
      type = TypeOfText.KEYWORD_LIGHT;
    }

    return type;
  }
}
