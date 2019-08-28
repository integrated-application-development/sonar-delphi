package org.sonar.plugins.delphi.antlr.ast.node;

import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.DelphiEnumerationType;
import org.sonar.plugins.delphi.type.Type;

public final class SubRangeTypeNode extends TypeNode {
  public SubRangeTypeNode(Token token) {
    super(token);
  }

  public SubRangeTypeNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  private ExpressionNode getLowExpression() {
    return (ExpressionNode) jjtGetChild(0);
  }

  private ExpressionNode getHighExpression() {
    return (ExpressionNode) jjtGetChild(1);
  }

  @Override
  @NotNull
  public Type createType() {
    ExpressionNode low = getLowExpression();
    ExpressionNode high = getHighExpression();

    return DelphiEnumerationType.subRange(
        DelphiEnumerationType.makeAnonymousImage(low.getImage(), high.getImage()), low.getType());
  }
}
