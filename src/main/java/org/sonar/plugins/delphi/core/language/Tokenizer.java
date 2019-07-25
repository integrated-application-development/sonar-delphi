package org.sonar.plugins.delphi.core.language;

import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.generated.DelphiLexer;

public class Tokenizer {

  /**
   * Create tokens from text.
   *
   * @param source The source code to parse for tokens
   * @return List of found tokens
   */
  public List<Token> tokenize(String[] source) {
    List<Token> tokens = new ArrayList<>();

    for (String string : source) {
      DelphiLexer lexer = new DelphiLexer(new ANTLRStringStream(string));
      Token token = lexer.nextToken();
      token.setText(token.getText().toLowerCase());
      while (token.getType() != Token.EOF) {
        tokens.add(token);
        token = lexer.nextToken();
      }
    }
    // has been changed to add compatibility for SonarQube 5.2
    tokens.add(new CommonToken(Token.EOF));
    return tokens;
  }
}
