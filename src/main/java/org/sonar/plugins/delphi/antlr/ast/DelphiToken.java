package org.sonar.plugins.delphi.antlr.ast;

import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.ast.GenericToken;
import org.antlr.runtime.Token;
import org.sonar.api.batch.sensor.highlighting.TypeOfText;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.core.DelphiKeywords;
import org.sonar.plugins.delphi.pmd.FilePosition;
import org.sonarsource.analyzer.commons.TokenLocation;

public class DelphiToken implements GenericToken {
  public static final String STRING_LITERAL = "STRING_LITERAL";
  public static final String NUMERIC_LITERAL = "NUMERIC_LITERAL";

  private final Token token;
  private String image;
  private Integer beginLine;
  private Integer beginColumn;
  private Integer endLine;
  private Integer endColumn;

  public DelphiToken(final Token token) {
    this.token = token;
  }

  @Override
  public String getImage() {
    if (image == null && !isNil()) {
      image = token.getText();
    }
    return image;
  }

  @Override
  public int getBeginLine() {
    if (beginLine == null) {
      calculatePosition();
    }
    return beginLine;
  }

  @Override
  public int getBeginColumn() {
    if (beginColumn == null) {
      calculatePosition();
    }
    return beginColumn;
  }

  @Override
  public int getEndLine() {
    if (endLine == null) {
      calculatePosition();
    }
    return endLine;
  }

  @Override
  public int getEndColumn() {
    if (endColumn == null) {
      calculatePosition();
    }
    return endColumn;
  }

  private void calculatePosition() {
    if (isComment()) {
      TokenLocation location =
          new TokenLocation(token.getLine(), token.getCharPositionInLine(), token.getText());
      beginLine = location.startLine();
      beginColumn = location.startLineOffset();
      endLine = location.endLine();
      endColumn = location.endLineOffset();
    } else {
      beginLine = token.getLine();
      beginColumn = token.getCharPositionInLine();
      endLine = beginLine;
      endColumn = beginColumn + getImage().length();
    }
  }

  @Override
  public GenericToken getNext() {
    throw new UnsupportedOperationException("Out of scope for antlr tokens");
  }

  @Override
  public GenericToken getPreviousComment() {
    throw new UnsupportedOperationException("Out of scope for antlr tokens");
  }

  public boolean isEof() {
    return !isNil() && token.getType() == Token.EOF;
  }

  public boolean isImaginary() {
    return isNil() || token.getLine() == FilePosition.UNDEFINED_LINE;
  }

  private boolean isStringLiteral() {
    return token.getType() == DelphiLexer.QuotedString;
  }

  private boolean isNumericLiteral() {
    return token.getType() == DelphiLexer.TkIntNum
        || token.getType() == DelphiLexer.TkRealNum
        || token.getType() == DelphiLexer.TkHexNum;
  }

  public boolean isWhitespace() {
    return token.getType() == DelphiLexer.WS;
  }

  public boolean isComment() {
    return token.getType() == DelphiLexer.COMMENT;
  }

  private boolean isKeyword() {
    return DelphiKeywords.KEYWORDS.contains(token.getType());
  }

  private boolean isSpecialKeyword() {
    return DelphiKeywords.SPECIAL_KEYWORDS.contains(token.getType());
  }

  public boolean isNil() {
    return token == null;
  }

  public int getIndex() {
    return isNil() ? -1 : token.getTokenIndex();
  }

  public int getType() {
    return isNil() ? Token.INVALID_TOKEN_TYPE : token.getType();
  }

  public Token getAntlrToken() {
    return token;
  }

  public String getNormalizedImage() {
    if (isStringLiteral()) {
      return STRING_LITERAL;
    }

    if (isNumericLiteral()) {
      return NUMERIC_LITERAL;
    }

    return token.getText().toLowerCase();
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
