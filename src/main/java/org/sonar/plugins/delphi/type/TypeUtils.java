package org.sonar.plugins.delphi.type;

import org.sonar.plugins.delphi.type.Type.PointerType;
import org.sonar.plugins.delphi.type.Type.StringType;
import org.sonar.plugins.delphi.type.Type.SubrangeType;
import org.sonar.plugins.delphi.type.Type.TypeType;

public final class TypeUtils {
  private TypeUtils() {
    // utility class
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

  public static Type dereference(Type type) {
    Type baseType = findBaseType(type);
    if (baseType instanceof PointerType) {
      return ((PointerType) baseType).dereferencedType();
    }
    return type;
  }

  /**
   * Check if the supplied type is a string with single-byte characters
   *
   * @param type The type we're checking
   * @return true if this is a string type with single-byte characters
   */
  public static boolean isNarrowString(Type type) {
    return type.isString() && ((StringType) type).characterType().size() == 1;
  }

  /**
   * Check if the supplied type is a string with multi-byte characters
   *
   * @param type The type we're checking
   * @return true if this is a string type with multi-byte characters
   */
  public static boolean isWideString(Type type) {
    return type.isString() && ((StringType) type).characterType().size() > 1;
  }
}
