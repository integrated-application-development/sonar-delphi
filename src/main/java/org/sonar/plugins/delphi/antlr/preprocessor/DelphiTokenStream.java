package org.sonar.plugins.delphi.antlr.preprocessor;

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
   * <p>NOTE: This refuses to compile without an unchecked conversion suppression. It's redundant
   * and patently unnecessary, but it makes javac happy.
   *
   * @return Token list
   */
  @SuppressWarnings({"unchecked", "RedundantSuppression"})
  @Override
  public List<Token> getTokens() {
    return tokens;
  }

  public void setTokens(List<Token> tokens) {
    this.tokens = tokens;
  }
}
