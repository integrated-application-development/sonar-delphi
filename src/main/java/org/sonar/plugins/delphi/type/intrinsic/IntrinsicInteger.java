package org.sonar.plugins.delphi.type.intrinsic;

import java.util.Arrays;
import org.sonar.plugins.delphi.type.Type.IntegerType;

public enum IntrinsicInteger {
  SHORTINT("ShortInt", 1, true),
  BYTE("Byte", 1, false),
  SMALLINT("SmallInt", 2, true),
  WORD("Word", 2, false),
  INTEGER("Integer", 4, true),
  LONGINT("LongInt", 4, true),
  FIXEDINT("FixedInt", 4, true),
  NATIVEINT("NativeInt", 4, true),
  LONGWORD("LongWord", 4, false),
  CARDINAL("Cardinal", LONGWORD.type),
  FIXEDUINT("FixedUInt", 4, false),
  NATIVEUINT("NativeUInt", 4, false),
  INT64("Int64", 8, true),
  UINT64("UInt64", 8, false);

  public final String image;
  public final IntegerType type;

  IntrinsicInteger(String image, int size, boolean signed) {
    this.image = image;
    this.type = new DelphiIntegerType(image, size, signed);
  }

  IntrinsicInteger(String image, IntegerType type) {
    this.image = image;
    this.type = type;
  }

  public static IntegerType fromLiteralValue(long value) {
    return Arrays.stream(IntrinsicInteger.values())
        .map(intrinsic -> intrinsic.type)
        .filter(
            type ->
                type.min().longValue() <= value
                    && ((value < 0) || Long.compareUnsigned(type.max().longValue(), value) >= 0))
        .findFirst()
        .orElseThrow(IllegalStateException::new);
  }
}
