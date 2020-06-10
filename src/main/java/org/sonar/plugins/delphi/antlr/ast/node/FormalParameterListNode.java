package org.sonar.plugins.delphi.antlr.ast.node;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;

public final class FormalParameterListNode extends DelphiNode {
  private List<FormalParameterData> parameters;
  private List<Type> parameterTypes;
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

  public List<FormalParameterData> getParameters() {
    if (parameters == null) {
      var builder = new ImmutableList.Builder<FormalParameterData>();
      for (FormalParameterNode parameterNode : findChildrenOfType(FormalParameterNode.class)) {
        builder.addAll(parameterNode.getParameters());
      }
      parameters = builder.build();
    }
    return parameters;
  }

  public List<Type> getParameterTypes() {
    if (parameterTypes == null) {
      parameterTypes =
          getParameters().stream()
              .map(FormalParameterData::getType)
              .collect(Collectors.toUnmodifiableList());
    }
    return parameterTypes;
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder imageBuilder = new StringBuilder();
      for (FormalParameterData parameter : getParameters()) {
        if (imageBuilder.length() != 0) {
          imageBuilder.append(';');
        }
        imageBuilder.append(parameter.getType().getImage());
      }
      image = imageBuilder.toString();
    }

    return image;
  }
}
