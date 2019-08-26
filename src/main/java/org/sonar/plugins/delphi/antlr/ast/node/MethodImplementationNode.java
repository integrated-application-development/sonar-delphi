package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class MethodImplementationNode extends MethodNode {
  private MethodBodyNode methodBody;

  public MethodImplementationNode(Token token) {
    super(token);
  }

  public MethodImplementationNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public boolean hasMethodBody() {
    return getMethodBody() != null;
  }

  public MethodBodyNode getMethodBody() {
    if (methodBody == null) {
      methodBody = getFirstChildOfType(MethodBodyNode.class);
    }
    return methodBody;
  }
}
