package org.sonar.plugins.delphi.antlr.ast.node;

import java.math.BigInteger;
import org.antlr.runtime.Token;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.DelphiLexer;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;

public final class IntegerLiteralNode extends LiteralNode {
  private String image;
  private Type type;

  public IntegerLiteralNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public BigInteger getValueAsBigInteger() {
    return new BigInteger(getImage(), getRadix());
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = getToken().getImage();
      image = StringUtils.remove(image, '_');
      switch (getToken().getType()) {
        case DelphiLexer.TkHexNum:
          image = StringUtils.removeStart(image, "$");
          break;
        case DelphiLexer.TkBinaryNum:
          image = StringUtils.removeStart(image, "%");
          break;
        default:
          // do nothing
      }
    }
    return image;
  }

  public int getRadix() {
    switch (getToken().getType()) {
      case DelphiLexer.TkHexNum:
        return 16;
      case DelphiLexer.TkBinaryNum:
        return 2;
      default:
        return 10;
    }
  }

  @Override
  @NotNull
  public Type getType() {
    if (type == null) {
      type = getTypeFactory().integerFromLiteralValue(this.getValueAsBigInteger());
    }
    return type;
  }
}
