package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class ArgumentListNode extends DelphiNode {
  private String image;
  private List<ExpressionNode> arguments;

  public ArgumentListNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<ExpressionNode> getArguments() {
    if (arguments == null) {
      arguments = findChildrenOfType(ExpressionNode.class);
    }
    return arguments;
  }

  public boolean isEmpty() {
    return getArguments().isEmpty();
  }

  @Override
  public String getImage() {
    if (image == null) {
      StringBuilder imageBuilder = new StringBuilder();
      imageBuilder.append("(");
      for (int i = 0; i < jjtGetNumChildren(); ++i) {
        imageBuilder.append(jjtGetChild(i).getImage());
      }
      image = imageBuilder.toString();
    }
    return image;
  }
}
