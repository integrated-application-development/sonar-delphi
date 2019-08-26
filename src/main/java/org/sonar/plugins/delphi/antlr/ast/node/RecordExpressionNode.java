package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class RecordExpressionNode extends ExpressionNode {
  private String image;

  public RecordExpressionNode(Token token) {
    super(token);
  }

  public RecordExpressionNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<RecordExpressionItemNode> getItems() {
    return findChildrenOfType(RecordExpressionItemNode.class);
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder imageBuilder = new StringBuilder();
      imageBuilder.append("(");
      for (RecordExpressionItemNode item : getItems()) {
        imageBuilder.append(item.getImage());
      }
      imageBuilder.append(")");
      image = imageBuilder.toString();
    }
    return image;
  }
}
