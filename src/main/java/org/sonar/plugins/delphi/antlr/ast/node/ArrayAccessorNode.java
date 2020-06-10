package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;

public final class ArrayAccessorNode extends DelphiNode {
  private DelphiNameOccurrence implicitNameOccurrence;

  public ArrayAccessorNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public List<ExpressionNode> getExpressions() {
    return findChildrenOfType(ExpressionNode.class);
  }

  public void setImplicitNameOccurrence(DelphiNameOccurrence implicitNameOccurrence) {
    this.implicitNameOccurrence = implicitNameOccurrence;
  }

  @Nullable
  public DelphiNameOccurrence getImplicitNameOccurrence() {
    return implicitNameOccurrence;
  }

  @Override
  public String getImage() {
    return "["
        + getExpressions().stream().map(ExpressionNode::getImage).collect(Collectors.joining(", "))
        + "]";
  }
}
