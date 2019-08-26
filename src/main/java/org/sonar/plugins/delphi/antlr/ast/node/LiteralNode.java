package org.sonar.plugins.delphi.antlr.ast.node;

import java.math.BigInteger;
import java.util.Locale;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;

public final class LiteralNode extends DelphiNode {
  private String image;

  public LiteralNode(Token token) {
    super(token);
  }

  public LiteralNode(int tokenType) {
    super(tokenType);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  public boolean isStringLiteral() {
    return jjtGetId() == DelphiLexer.TkStringLiteral;
  }

  public boolean isNilLiteral() {
    return jjtGetId() == DelphiLexer.NIL;
  }

  public boolean isIntegerLiteral() {
    return jjtGetId() == DelphiLexer.TkIntNum;
  }

  public boolean isHexadecimalLiteral() {
    return jjtGetId() == DelphiLexer.TkHexNum || jjtGetId() == DelphiLexer.TkAsmHexNum;
  }

  public boolean isRealLiteral() {
    return jjtGetId() == DelphiLexer.TkRealNum;
  }

  public String getValueAsString() {
    return getImage();
  }

  public int getValueAsInt() {
    if (isIntegerLiteral()) {
      return (int) getValueAsLong();
    }
    return 0;
  }

  private int getIntBase() {
    return isHexadecimalLiteral() ? 16 : 10;
  }

  public long getValueAsLong() {
    if (isIntegerLiteral()) {
      BigInteger bigInt = new BigInteger(getImage(), getIntBase());
      return bigInt.longValue();
    }
    return 0L;
  }

  public double getValueAsDouble() {
    if (isRealLiteral()) {
      return Double.parseDouble(getImage());
    }
    return Double.NaN;
  }

  @Override
  public String getImage() {
    if (image == null) {
      if (isStringLiteral()) {
        StringBuilder imageBuilder = new StringBuilder();
        for (int i = 0; i < jjtGetNumChildren(); ++i) {
          imageBuilder.append(jjtGetChild(i).getImage());
        }
        image = imageBuilder.toString();
      } else {
        image = super.getImage().toLowerCase(Locale.ROOT);
        if (image.startsWith("$")) {
          image = image.substring(1);
        }
        if (image.endsWith("h")) {
          image = image.substring(0, image.length() - 1);
        }
      }
    }
    return image;
  }
}
