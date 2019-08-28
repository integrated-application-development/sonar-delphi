package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.BooleanType;
import org.sonar.plugins.delphi.type.DelphiPointerType;
import org.sonar.plugins.delphi.type.Type;

public final class UnaryExpressionNode extends ExpressionNode {
  private UnaryOp operator;
  private String image;

  public UnaryExpressionNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public UnaryOp getOperator() {
    if (operator == null) {
      operator = UnaryOp.from(jjtGetId());
    }
    return operator;
  }

  public ExpressionNode getExpression() {
    return (ExpressionNode) jjtGetChild(0);
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = getToken().getImage() + " " + getExpression().getImage();
    }
    return image;
  }

  @Override
  @NotNull
  public Type createType() {
    Type expressionType = getExpression().getType();
    switch (getOperator()) {
      case NOT:
        return BooleanType.BOOLEAN.type;
      case ADDRESS:
        return DelphiPointerType.pointerTo(expressionType);
      default:
        return expressionType;
    }
  }

  public enum UnaryOp {
    NOT(DelphiLexer.NOT),
    PLUS(DelphiLexer.PLUS),
    NEGATE(DelphiLexer.MINUS),
    ADDRESS(DelphiLexer.AT2);

    public final int tokenType;
    private static final Map<Integer, UnaryOp> TOKEN_TYPE_MAP = new HashMap<>();

    static {
      Arrays.asList(UnaryOp.values()).forEach(op -> TOKEN_TYPE_MAP.put(op.tokenType, op));
    }

    UnaryOp(int tokenType) {
      this.tokenType = tokenType;
    }

    @NotNull
    public static UnaryOp from(int tokenType) {
      return TOKEN_TYPE_MAP.get(tokenType);
    }
  }
}
