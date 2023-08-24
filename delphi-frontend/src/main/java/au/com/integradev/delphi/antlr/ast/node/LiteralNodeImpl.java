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

import java.math.BigInteger;
import org.antlr.runtime.Token;
import org.sonar.plugins.communitydelphi.api.ast.DecimalLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.IntegerLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.LiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.NilLiteralNode;
import org.sonar.plugins.communitydelphi.api.ast.TextLiteralNode;

public abstract class LiteralNodeImpl extends DelphiNodeImpl implements LiteralNode {
  protected LiteralNodeImpl(Token token) {
    super(token);
  }

  protected LiteralNodeImpl(int tokenType) {
    super(tokenType);
  }

  @Override
  public boolean isTextLiteral() {
    return this instanceof TextLiteralNode;
  }

  @Override
  public boolean isNilLiteral() {
    return this instanceof NilLiteralNode;
  }

  @Override
  public boolean isIntegerLiteral() {
    return this instanceof IntegerLiteralNode;
  }

  @Override
  public boolean isHexadecimalLiteral() {
    return this instanceof IntegerLiteralNode && ((IntegerLiteralNode) this).getRadix() == 16;
  }

  @Override
  public boolean isBinaryLiteral() {
    return this instanceof IntegerLiteralNode && ((IntegerLiteralNode) this).getRadix() == 2;
  }

  @Override
  public boolean isDecimalLiteral() {
    return this instanceof DecimalLiteralNode;
  }

  @Override
  public String getValueAsString() {
    return getImage();
  }

  @Override
  public int getValueAsInt() {
    return getValueAsBigInteger().intValue();
  }

  @Override
  public BigInteger getValueAsBigInteger() {
    return BigInteger.ZERO;
  }

  @Override
  public double getValueAsDouble() {
    return Double.NaN;
  }
}
