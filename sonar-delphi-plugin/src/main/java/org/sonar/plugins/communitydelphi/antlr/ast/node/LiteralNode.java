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
import org.sonar.plugins.communitydelphi.type.Typed;

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
