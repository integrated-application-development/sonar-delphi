package org.sonar.plugins.delphi.antlr.ast.node;

import java.math.BigInteger;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.type.Typed;

public abstract class LiteralNode extends DelphiNode implements Typed {
  protected LiteralNode(Token token) {
    super(token);
  }

  protected LiteralNode(int tokenType) {
    super(tokenType);
  }

  public boolean isTextLiteral() {
    return this instanceof TextLiteralNode;
  }

  public boolean isNilLiteral() {
    return this instanceof NilLiteralNode;
  }

  public boolean isIntegerLiteral() {
    return this instanceof IntegerLiteralNode;
  }

  public boolean isHexadecimalLiteral() {
    return this instanceof IntegerLiteralNode && ((IntegerLiteralNode) this).getRadix() == 16;
  }

  public boolean isBinaryLiteral() {
    return this instanceof IntegerLiteralNode && ((IntegerLiteralNode) this).getRadix() == 2;
  }

  public boolean isDecimalLiteral() {
    return this instanceof DecimalLiteralNode;
  }

  public String getValueAsString() {
    return getImage();
  }

  public int getValueAsInt() {
    return getValueAsBigInteger().intValue();
  }

  public BigInteger getValueAsBigInteger() {
    return BigInteger.ZERO;
  }

  public double getValueAsDouble() {
    return Double.NaN;
  }
}
