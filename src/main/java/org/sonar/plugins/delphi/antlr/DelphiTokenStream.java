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
package org.sonar.plugins.delphi.antlr;

import java.util.List;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenRewriteStream;
import org.antlr.runtime.TokenSource;

public class DelphiTokenStream extends TokenRewriteStream {
  public DelphiTokenStream(TokenSource tokenSource) {
    super(tokenSource);
  }

  /**
   * Returns the underlying token list. This overrides the ANTLR getTokens function which normally
   * returns a raw list.
   *
   * <p>This requires a suppression because a generic specialization is not a covariant return type
   * in the traditional sense.
   *
   * @return Token list
   */
  @SuppressWarnings("unchecked")
  @Override
  public List<Token> getTokens() {
    return tokens;
  }

  public void setTokens(List<Token> tokens) {
    this.tokens = tokens;
  }
}
