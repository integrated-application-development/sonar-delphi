package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.ArrayList;
import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameter;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class FormalParameterListNode extends DelphiNode {
  private List<FormalParameter> parameters;
  private String image;

  public FormalParameterListNode(Token token) {
    super(token);
  }

  public FormalParameterListNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<FormalParameter> getParameters() {
    if (parameters == null) {
      parameters = new ArrayList<>();
      for (FormalParameterNode parameterNode : findChildrenOfType(FormalParameterNode.class)) {
        parameters.addAll(parameterNode.getParameters());
      }
    }
    return parameters;
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder imageBuilder = new StringBuilder();
      for (FormalParameter parameter : getParameters()) {
        imageBuilder.append(parameter.getTypeImage());
        imageBuilder.append(';');
      }
      image = imageBuilder.toString();
    }

    return image;
  }
}
