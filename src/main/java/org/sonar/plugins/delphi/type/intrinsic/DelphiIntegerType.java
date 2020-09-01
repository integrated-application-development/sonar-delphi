package org.sonar.plugins.delphi.type.intrinsic;

import com.google.common.math.BigIntegerMath;
import com.google.errorprone.annotations.Immutable;
import java.math.BigInteger;
import java.math.RoundingMode;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type.IntegerType;

@Immutable
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
