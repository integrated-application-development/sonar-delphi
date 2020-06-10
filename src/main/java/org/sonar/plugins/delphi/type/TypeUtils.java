package org.sonar.plugins.delphi.type;

import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.ANSICHAR;
import static org.sonar.plugins.delphi.type.intrinsic.IntrinsicText.WIDECHAR;

import org.sonar.plugins.delphi.type.Type.BooleanType;
import org.sonar.plugins.delphi.type.Type.IntegerType;
import org.sonar.plugins.delphi.type.Type.SubrangeType;
import org.sonar.plugins.delphi.type.Type.TypeType;

public final class TypeUtils {
  private TypeUtils() {
    // utility class
  }

  /**
   * If type is an ordinal type, returns the ordinal size in bytes.
   *
   * @param type The type having its ordinal size checked
   * @return Ordinal size in bytes, or 0 if type is not an ordinal type.
   */
  public static int ordinalSize(Type type) {
    if (type.isInteger()) {
      return ((IntegerType) type).size();
    } else if (type.isBoolean()) {
      return ((BooleanType) type).size();
    } else if (type.is(WIDECHAR.type)) {
      return 2;
    } else if (type.is(ANSICHAR.type)) {
      return 1;
    } else {
      return 0;
    }
  }

  public static Type findBaseType(Type type) {
    while (type.isTypeType()) {
      type = ((TypeType) type).originalType();
    }

    if (type.isSubrange()) {
      type = ((SubrangeType) type).hostType();
    }

    return type;
  }
}
