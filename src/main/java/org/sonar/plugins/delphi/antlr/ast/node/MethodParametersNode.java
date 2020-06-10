package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.Collections;
import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;

public final class MethodParametersNode extends DelphiNode {
  private String image;
  private FormalParameterListNode parameterList;

  public MethodParametersNode(Token token) {
    super(token);
  }

  public MethodParametersNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<FormalParameterData> getParameters() {
    return isEmpty() ? Collections.emptyList() : getFormalParametersList().getParameters();
  }

  public List<Type> getParameterTypes() {
    return isEmpty() ? Collections.emptyList() : getFormalParametersList().getParameterTypes();
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = "(" + (isEmpty() ? "" : getFormalParametersList().getImage()) + ")";
    }
    return image;
  }

  public FormalParameterListNode getFormalParametersList() {
    if (parameterList == null && !isEmpty()) {
      parameterList = (FormalParameterListNode) jjtGetChild(1);
    }
    return parameterList;
  }

  public boolean isEmpty() {
    return jjtGetNumChildren() < 3;
  }
}
