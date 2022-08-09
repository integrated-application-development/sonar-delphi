package org.sonar.plugins.delphi.pmd.rules;

import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.TypeUtils;

public class CharacterToCharacterPointerCastRule extends AbstractCastRule {
  @Override
  protected boolean isViolation(Type originalType, Type castType) {
    return TypeUtils.findBaseType(originalType).isChar()
        && TypeUtils.findBaseType(castType).isPointer()
        && TypeUtils.findBaseType(TypeUtils.dereference(castType)).isChar();
  }
}
