package org.sonar.plugins.delphi.type;

import com.google.common.math.BigIntegerMath;
import com.google.errorprone.annotations.Immutable;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;

@Immutable
@SuppressWarnings("Immutable")
public class DelphiIntrinsicType extends DelphiType {
  public static final Type TVARREC = new DelphiIntrinsicType("TVarRec");

  public enum IntegerType {
    BYTE("Byte", 1, false),
    SHORTINT("ShortInt", 1, true),
    WORD("Word", 2, false),
    SMALLINT("SmallInt", 2, true),
    CARDINAL("Cardinal", 4, false),
    LONGWORD("LongWord", 4, false),
    FIXEDUINT("FixedUInt", 4, false),
    INTEGER("Integer", 4, true),
    LONGINT("LongInt", 4, true),
    FIXEDINT("FixedInt", 4, true),
    UINT64("UInt64", 8, false),
    INT64("Int64", 8, true),
    NATIVEUINT("NativeUInt", 4, false),
    NATIVEINT("NativeInt", 4, true);

    public final DelphiIntrinsicType type;
    private final BigInteger min;
    private final BigInteger max;
    private final int size;

    IntegerType(String image, int size, boolean signed) {
      this.type = new DelphiIntrinsicType(image);
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

    public boolean isSigned() {
      return !min.equals(BigInteger.ZERO);
    }

    public boolean isWithinLimit(IntegerType other) {
      return size <= other.size;
    }

    public boolean isSameRange(IntegerType other) {
      return min.equals(other.min) && max.equals(other.max);
    }

    public double ordinalDistance(IntegerType other) {
      BigInteger minDistance = min.subtract(other.min).abs();
      BigInteger maxDistance = max.subtract(other.max).abs();
      return minDistance.add(maxDistance).doubleValue();
    }

    public static IntegerType fromType(Type type) {
      return Arrays.stream(IntegerType.values())
          .filter(typeData -> typeData.type.is(type))
          .findFirst()
          .orElse(null);
    }

    public static IntegerType fromLiteralValue(long value) {
      return Arrays.stream(values())
          .filter(
              intType ->
                  intType.min.longValue() <= value
                      && ((value < 0) || Long.compareUnsigned(intType.max.longValue(), value) >= 0))
          .findFirst()
          .orElseThrow(IllegalStateException::new);
    }
  }

  public enum DecimalType {
    SINGLE("Single", 4),
    DOUBLE("Double", 8),
    REAL("Real", 8),
    EXTENDED("Extended", 10),
    REAL48("Real48", 6),
    COMP("Comp", 8),
    CURRENCY("Currency", 8);

    public final DelphiIntrinsicType type;
    public final int size;

    DecimalType(String image, int size) {
      this.type = new DelphiIntrinsicType(image);
      this.size = size;
    }

    public static DecimalType fromType(Type type) {
      return Arrays.stream(DecimalType.values())
          .filter(typeData -> typeData.type.is(type))
          .findFirst()
          .orElse(null);
    }
  }

  public enum BooleanType {
    BOOLEAN("Boolean", 1),
    BYTEBOOL("ByteBool", 1),
    WORDBOOL("WordBool", 2),
    LONGBOOL("LongBool", 4);

    public final DelphiIntrinsicType type;
    public final int size;

    BooleanType(String image, int size) {
      this.type = new DelphiIntrinsicType(image);
      this.size = size;
    }

    public static BooleanType fromType(Type type) {
      return Arrays.stream(BooleanType.values())
          .filter(typeData -> typeData.type.is(type))
          .findFirst()
          .orElse(null);
    }
  }

  public enum TextType {
    ANSICHAR("AnsiChar"),
    CHAR("Char"),
    WIDECHAR("WideChar"),
    ANSISTRING("AnsiString"),
    RAWBYTESTRING("RawByteString"),
    UNICODESTRING("UnicodeString"),
    STRING("String"),
    SHORTSTRING("ShortString"),
    WIDESTRING("WideString");

    public final DelphiIntrinsicType type;

    TextType(String image) {
      this.type = new DelphiIntrinsicType(image);
    }
  }

  protected DelphiIntrinsicType(String image) {
    super(image);
  }
}
