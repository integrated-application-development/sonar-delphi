package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;

public abstract class ForLoopVarNode extends DelphiNode {
  protected ForLoopVarNode(Token token) {
    super(token);
  }

  protected ForLoopVarNode(int tokenType) {
    super(tokenType);
  }
}
