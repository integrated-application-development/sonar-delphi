package org.sonar.plugins.delphi.antlr.ast.node;

import com.google.common.base.Preconditions;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.BooleanType;
import org.sonar.plugins.delphi.type.DelphiIntrinsicType.TextType;
import org.sonar.plugins.delphi.type.Type;

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
      image = getLeft().getImage() + " " + getToken().getImage() + " " + getRight().getImage();
    }
    return image;
  }

  @Override
  @NotNull
  public Type createType() {
    if (getOperator().isLogicalOperator) {
      return BooleanType.BOOLEAN.type;
    }

    Type type = getLeft().getType();

    if (type.isUnknown()) {
      type = getRight().getType();
    }

    if (type.is(TextType.CHAR.type)) {
      // Assume this expression is a string concatenation.
      type = TextType.STRING.type;
    }

    return type;
  }

  public enum BinaryOp {
    AND(DelphiLexer.AND, true),
    OR(DelphiLexer.OR, true),
    EQUAL(DelphiLexer.EQUAL, true),
    GREATER_THAN(DelphiLexer.GT, true),
    LESS_THAN(DelphiLexer.LT, true),
    GREATER_THAN_EQUAL(DelphiLexer.GE, true),
    LESS_THAN_EQUAL(DelphiLexer.LE, true),
    NOT_EQUAL(DelphiLexer.NOT_EQUAL, true),
    IN(DelphiLexer.IN, true),
    IS(DelphiLexer.IS, true),
    XOR(DelphiLexer.XOR),
    ADD(DelphiLexer.PLUS),
    SUBTRACT(DelphiLexer.MINUS),
    MULTIPLY(DelphiLexer.STAR),
    DIVIDE(DelphiLexer.SLASH),
    DIV(DelphiLexer.DIV),
    MOD(DelphiLexer.MOD),
    SHL(DelphiLexer.SHL),
    SHR(DelphiLexer.SHR),
    AS(DelphiLexer.AS);

    private final int tokenType;
    public final boolean isLogicalOperator;
    private static final Map<Integer, BinaryOp> TOKEN_TYPE_MAP = new HashMap<>();

    static {
      Arrays.asList(BinaryOp.values()).forEach(op -> TOKEN_TYPE_MAP.put(op.tokenType, op));
    }

    BinaryOp(int tokenType) {
      this(tokenType, false);
    }

    BinaryOp(int tokenType, boolean isLogicalOperator) {
      this.tokenType = tokenType;
      this.isLogicalOperator = isLogicalOperator;
    }

    public static BinaryOp from(int tokenType) {
      return Preconditions.checkNotNull(TOKEN_TYPE_MAP.get(tokenType));
    }
  }
}
