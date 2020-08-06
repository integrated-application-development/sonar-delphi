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
    return this instanceof HexLiteralNode;
  }

  public boolean isDecimalLiteral() {
    return this instanceof DecimalLiteralNode;
  }

  public String getValueAsString() {
    return getImage();
  }

  public int getValueAsInt() {
    return (int) getValueAsLong();
  }

  public long getValueAsLong() {
    return 0L;
  }

  public double getValueAsDouble() {
    return Double.NaN;
  }

  protected static long parseImage(String image, int base) {
    BigInteger bigInt = new BigInteger(image, base);
    return bigInt.longValue();
  }
}
