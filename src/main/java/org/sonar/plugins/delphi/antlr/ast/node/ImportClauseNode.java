package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;

public abstract class ImportClauseNode extends DelphiNode {
  protected ImportClauseNode(Token token) {
    super(token);
  }
}
