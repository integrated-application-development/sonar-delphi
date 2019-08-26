package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;

public abstract class FileHeaderNode extends DelphiNode {
  FileHeaderNode(Token token) {
    super(token);
  }

  public QualifiedIdentifierNode getQualifiedIdentifier() {
    return (QualifiedIdentifierNode) jjtGetChild(0);
  }

  public String getName() {
    return getQualifiedIdentifier().getQualifiedName();
  }
}
