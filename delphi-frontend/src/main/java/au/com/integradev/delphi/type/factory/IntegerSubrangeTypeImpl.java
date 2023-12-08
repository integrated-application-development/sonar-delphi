/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
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
package au.com.integradev.delphi.type.factory;

import java.math.BigInteger;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.IntegerSubrangeType;

public class IntegerSubrangeTypeImpl extends SubrangeTypeImpl implements IntegerSubrangeType {
  private final BigInteger min;
  private final BigInteger max;

  IntegerSubrangeTypeImpl(String image, Type hostType, BigInteger min, BigInteger max) {
    super(image, hostType);
    this.min = min;
    this.max = max;
  }

  @Override
  public BigInteger min() {
    return min;
  }

  @Override
  public BigInteger max() {
    return max;
  }

  @Override
  public boolean isSigned() {
    return min.compareTo(BigInteger.ZERO) < 0;
  }

  @Override
  public boolean isInteger() {
    return true;
  }
}
