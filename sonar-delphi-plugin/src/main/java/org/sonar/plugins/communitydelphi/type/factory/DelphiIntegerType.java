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
package org.sonar.plugins.communitydelphi.type.factory;

import com.google.common.math.BigIntegerMath;
import java.math.BigInteger;
import java.math.RoundingMode;
import org.sonar.plugins.communitydelphi.type.DelphiType;
import org.sonar.plugins.communitydelphi.type.Type.IntegerType;

class DelphiIntegerType extends DelphiType implements IntegerType {
  private final String image;
  private final BigInteger min;
  private final BigInteger max;
  private final int size;

  DelphiIntegerType(String image, int size, boolean signed) {
    this.image = image;
    this.size = size;
    BigInteger capacity = BigInteger.valueOf(256).pow(size).subtract(BigInteger.ONE);
    if (signed) {
      min = BigIntegerMath.divide(capacity, BigInteger.TWO, RoundingMode.UP).negate();
      max = BigIntegerMath.divide(capacity, BigInteger.TWO, RoundingMode.DOWN);
    } else {
      min = BigInteger.ZERO;
      max = capacity;
    }
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public int size() {
    return size;
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
    return !min.equals(BigInteger.ZERO);
  }

  @Override
  public boolean isWithinLimit(IntegerType other) {
    return size <= other.size();
  }

  @Override
  public boolean isSameRange(IntegerType other) {
    return min.equals(other.min()) && max.equals(other.max());
  }

  @Override
  public double ordinalDistance(IntegerType other) {
    BigInteger minDistance = min.subtract(other.min()).abs();
    BigInteger maxDistance = max.subtract(other.max()).abs();
    return minDistance.add(maxDistance).doubleValue();
  }

  @Override
  public boolean isInteger() {
    return true;
  }
}
