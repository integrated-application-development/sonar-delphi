package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;

public abstract class StatementNode extends DelphiNode {
  StatementNode(Token token) {
    super(token);
  }

  StatementNode(int tokenType) {
    super(tokenType);
  }
}
