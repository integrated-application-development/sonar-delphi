/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.delphi.antlr.ast.token;

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
    if (isIncludeToken()) {
      FilePosition insertionPosition = ((IncludeToken) token).getInsertionPosition();
      beginLine = insertionPosition.getBeginLine();
      beginColumn = insertionPosition.getBeginColumn();
      endLine = insertionPosition.getEndLine();
      endColumn = insertionPosition.getEndColumn();
    } else if (isComment()) {
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
    return token.getType() == DelphiLexer.TkQuotedString;
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

  public boolean isCompilerDirective() {
    return token.getType() == DelphiLexer.TkCompilerDirective;
  }

  private boolean isKeyword() {
    return DelphiKeywords.KEYWORDS.contains(token.getType());
  }

  private boolean isIncludeToken() {
    return token instanceof IncludeToken;
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
    } else if (isCompilerDirective()) {
      type = TypeOfText.PREPROCESS_DIRECTIVE;
    } else if (isKeyword()) {
      type = TypeOfText.KEYWORD;
    }

    return type;
  }
}
