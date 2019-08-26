package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ArrayExpressionNode extends ExpressionNode {
  private String image;

  public ArrayExpressionNode(Token token) {
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
      StringBuilder imageBuilder = new StringBuilder();
      imageBuilder.append("(");
      for (ExpressionNode element : getElements()) {
        imageBuilder.append(element.getImage());
      }
      imageBuilder.append(")");
      image = imageBuilder.toString();
    }
    return image;
  }
}
