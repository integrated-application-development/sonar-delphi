package org.sonar.plugins.delphi.pmd.rules;

import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.StringType;
import org.sonar.plugins.delphi.type.TypeUtils;

public class UnicodeToAnsiCastRule extends AbstractCastRule {
  @Override
  protected boolean isViolation(Type originalType, Type castType) {
    return isWideTextType(originalType) && isNarrowTextType(castType);
  }

  private static boolean isWideTextType(Type type) {
    return characterSize(type) > 1;
  }

  private static boolean isNarrowTextType(Type type) {
    return characterSize(type) == 1;
  }

  private static int characterSize(Type type) {
    type = TypeUtils.findBaseType(type);
    if (type.isString()) {
      type = ((StringType) type).characterType();
    }
    if (!type.isChar()) {
      type = DelphiType.unknownType();
    }
    return type.size();
  }
}
