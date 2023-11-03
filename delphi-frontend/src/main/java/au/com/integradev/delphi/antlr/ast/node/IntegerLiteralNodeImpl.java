/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.antlr.ast.visitors.DelphiParserVisitor;
import java.math.BigInteger;
import org.antlr.runtime.Token;
import org.apache.commons.lang3.StringUtils;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.type.Type;

public final class IntegerLiteralNodeImpl extends DelphiNodeImpl implements IntegerLiteralNode {
  private Type type;

  public IntegerLiteralNodeImpl(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Override
  public BigInteger getValue() {
    return new BigInteger(getDigits(), getRadix());
  }

  @Override
  public int getRadix() {
    switch (getTokenType()) {
      case HEX_NUMBER:
        return 16;
      case BINARY_NUMBER:
        return 2;
      default:
        return 10;
    }
  }

  @Override
  public String getDigits() {
    String digits = getImage();
    digits = StringUtils.remove(digits, '_');
    switch (getTokenType()) {
      case HEX_NUMBER:
        digits = StringUtils.removeStart(digits, "$");
        break;
      case BINARY_NUMBER:
        digits = StringUtils.removeStart(digits, "%");
        break;
      default:
        // do nothing
    }
    return digits;
  }

  @Override
  public Type getType() {
    if (type == null) {
      type = getTypeFactory().integerFromLiteralValue(this.getValue());
    }
    return type;
  }
}
