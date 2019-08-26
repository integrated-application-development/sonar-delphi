package org.sonar.plugins.delphi.antlr.ast.node;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class BinaryExpressionNode extends ExpressionNode {
  private String image;
  private BinaryOp operator;

  public BinaryExpressionNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public ExpressionNode getLeft() {
    return (ExpressionNode) jjtGetChild(0);
  }

  public ExpressionNode getRight() {
    return (ExpressionNode) jjtGetChild(1);
  }

  public BinaryOp getOperator() {
    if (operator == null) {
      operator = BinaryOp.from(jjtGetId());
    }
    return operator;
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = getLeft().getImage() + " " + getOperator() + " " + getRight().getImage();
    }
    return image;
  }

  public enum BinaryOp {
    ADD(DelphiLexer.PLUS),
    SUBTRACT(DelphiLexer.MINUS),
    OR(DelphiLexer.OR),
    XOR(DelphiLexer.XOR),
    MULTIPLY(DelphiLexer.STAR),
    DIVIDE(DelphiLexer.SLASH),
    DIV(DelphiLexer.DIV),
    MOD(DelphiLexer.MOD),
    AND(DelphiLexer.AND),
    SHL(DelphiLexer.SHL),
    SHR(DelphiLexer.SHR),
    EQUAL(DelphiLexer.EQUAL),
    GREATER_THAN(DelphiLexer.GT),
    LESS_THAN(DelphiLexer.LT),
    GREATER_THAN_EQUAL(DelphiLexer.GE),
    LESS_THAN_EQUAL(DelphiLexer.LE),
    NOT_EQUAL(DelphiLexer.NOT_EQUAL),
    IN(DelphiLexer.IN),
    IS(DelphiLexer.IS),
    AS(DelphiLexer.AS);

    public final int tokenType;
    private static final Map<Integer, BinaryOp> TOKEN_TYPE_MAP = new HashMap<>();

    static {
      Arrays.asList(BinaryOp.values()).forEach(op -> TOKEN_TYPE_MAP.put(op.tokenType, op));
    }

    BinaryOp(int tokenType) {
      this.tokenType = tokenType;
    }

    public static BinaryOp from(int tokenType) {
      return Preconditions.checkNotNull(TOKEN_TYPE_MAP.get(tokenType));
    }
  }
}
