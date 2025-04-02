/*
 * Sonar Delphi Plugin
 * Copyright (C) 2025 Integrated Application Development
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
package au.com.integradev.delphi.preprocessor;

import au.com.integradev.delphi.antlr.ast.token.DelphiTokenImpl;
import au.com.integradev.delphi.utils.LocatableException;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.token.DelphiToken;

public class PreprocessorException extends RuntimeException implements LocatableException {
  private final DelphiToken token;

  public PreprocessorException(String message, DelphiToken token) {
    this(message, token, null);
  }

  public PreprocessorException(String message, DelphiToken token, Throwable cause) {
    super(createMessage(message, token), cause);
    this.token = token;
  }

  @Override
  public int getLine() {
    return token.getBeginLine();
  }

  private static String createMessage(String message, DelphiToken token) {
    Token rawToken = ((DelphiTokenImpl) token).getAntlrToken();
    int line = rawToken.getLine();
    int column = rawToken.getCharPositionInLine();

    message = String.format("line %d:%d %s", line, column, message);

    if (token.isIncludedToken()) {
      message = String.format("included on line %d :: %s", token.getBeginLine(), message);
    }

    return message;
  }
}
