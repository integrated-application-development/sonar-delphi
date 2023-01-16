/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.communitydelphi.antlr.ast.node;

import java.math.BigInteger;
import org.antlr.runtime.Token;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.communitydelphi.antlr.DelphiLexer;
import org.sonar.plugins.communitydelphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.communitydelphi.type.Type;

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
