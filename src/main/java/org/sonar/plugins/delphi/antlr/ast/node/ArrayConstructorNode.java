package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import java.util.stream.Collectors;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.DelphiArrayConstructorType;
import org.sonar.plugins.delphi.type.Type;

public final class ArrayConstructorNode extends ExpressionNode {
  private String image;

  public ArrayConstructorNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<ExpressionNode> getElements() {
    return findChildrenOfType(ExpressionNode.class);
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder builder = new StringBuilder();
      builder.append("[");
      for (int i = 0; i < jjtGetNumChildren() - 1; ++i) {
        if (i > 0) {
          builder.append(", ");
        }
        builder.append(jjtGetChild(i).getImage());
      }
      builder.append("]");
      image = builder.toString();
    }
    return image;
  }

  @Override
  @NotNull
  public Type createType() {
    return DelphiArrayConstructorType.arrayConstructor(
        getElements().stream()
            .map(ExpressionNode::getType)
            .collect(Collectors.toUnmodifiableList()));
  }
}
