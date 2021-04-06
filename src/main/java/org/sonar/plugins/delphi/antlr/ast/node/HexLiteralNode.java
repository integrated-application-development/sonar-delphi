package org.sonar.plugins.delphi.antlr.ast.node;

import java.math.BigInteger;
import org.antlr.runtime.Token;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;

public final class HexLiteralNode extends LiteralNode {
  private String image;
  private Type type;

  public HexLiteralNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public BigInteger getValueAsBigInteger() {
    return new BigInteger(getImage(), 16);
  }

  @Override
  public String getImage() {
    if (image == null) {
      image = getToken().getImage();
      image = StringUtils.removeStart(image, "$");
      image = StringUtils.removeEnd(image, "h");
    }
    return image;
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
