package org.sonar.plugins.delphi.antlr.ast.node;

import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.ast.Node;
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
    MethodParametersNode parameters = getMethodParametersNode();
    return parameters != null ? parameters.getImage() : "";
  }

  @Nullable
  public MethodParametersNode getMethodParametersNode() {
    Node node = jjtGetChild(0);
    return (node instanceof MethodParametersNode) ? (MethodParametersNode) node : null;
  }

  @Nullable
  public MethodReturnTypeNode getMethodReturnTypeNode() {
    Node node = jjtGetChild(hasMethodParametersNode() ? 1 : 0);
    return (node instanceof MethodReturnTypeNode) ? (MethodReturnTypeNode) node : null;
  }

  public boolean hasMethodParametersNode() {
    return getMethodParametersNode() != null;
  }
}
