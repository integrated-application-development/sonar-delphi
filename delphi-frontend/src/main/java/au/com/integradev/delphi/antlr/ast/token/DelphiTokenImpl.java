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
package au.com.integradev.delphi.antlr.ast.token;

import au.com.integradev.delphi.core.DelphiKeywords;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.check.FilePosition;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;
import org.sonar.plugins.communitydelphi.api.token.DelphiTokenType;
import org.sonarsource.analyzer.commons.TokenLocation;

public class DelphiTokenImpl implements DelphiToken {
  private final Token token;
  private final DelphiTokenType tokenType;
  private String image;
  private Integer beginLine;
  private Integer beginColumn;
  private Integer endLine;
  private Integer endColumn;

  public DelphiTokenImpl(Token token) {
    this.token = token;
    this.tokenType =
        token == null
            ? DelphiTokenType.INVALID
            : DelphiTokenTypeFactory.createTokenType(token.getType());
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
  public boolean isEof() {
    return !isNil() && token.getType() == Token.EOF;
  }

  @Override
  public boolean isImaginary() {
    return isNil() || token.getLine() == FilePosition.UNDEFINED_LINE;
  }

  @Override
  public boolean isWhitespace() {
    return tokenType == DelphiTokenType.WHITESPACE;
  }

  @Override
  public boolean isComment() {
    return tokenType == DelphiTokenType.COMMENT;
  }

  @Override
  public boolean isCompilerDirective() {
    return tokenType == DelphiTokenType.COMPILER_DIRECTIVE;
  }

  @Override
  public boolean isKeyword() {
    return DelphiKeywords.KEYWORDS.contains(tokenType);
  }

  private boolean isIncludeToken() {
    return token instanceof IncludeToken;
  }

  @Override
  public boolean isNil() {
    return token == null;
  }

  @Override
  public int getIndex() {
    return isNil() ? -1 : token.getTokenIndex();
  }

  @Override
  public DelphiTokenType getType() {
    return tokenType;
  }

  public Token getAntlrToken() {
    return token;
  }
}
