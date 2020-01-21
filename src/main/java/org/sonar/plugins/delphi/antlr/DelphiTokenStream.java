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
