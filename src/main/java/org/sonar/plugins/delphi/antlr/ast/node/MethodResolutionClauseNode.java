package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class MethodResolutionClauseNode extends DelphiNode {
  public MethodResolutionClauseNode(Token token) {
    super(token);
  }

  public MethodResolutionClauseNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public NameReferenceNode getInterfaceMethodNameNode() {
    return (NameReferenceNode) jjtGetChild(1);
  }

  public NameReferenceNode getImplementationMethodNameNode() {
    return (NameReferenceNode) jjtGetChild(2);
  }
}
