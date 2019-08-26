package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ProcedureTypeHeadingNode extends DelphiNode {
  public ProcedureTypeHeadingNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public String getImage() {
    return super.getImage() + getParameterSignature();
  }

  private String getParameterSignature() {
    return hasMethodParametersNode() ? getMethodParametersNode().getImage() : "";
  }

  private MethodParametersNode getMethodParametersNode() {
    return getFirstChildOfType(MethodParametersNode.class);
  }

  private boolean hasMethodParametersNode() {
    return getMethodParametersNode() != null;
  }
}
