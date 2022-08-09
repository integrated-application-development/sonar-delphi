package org.sonar.plugins.delphi.pmd.rules;

import org.sonar.plugins.delphi.type.Type;

public class RedundantCastRule extends AbstractCastRule {
  @Override
  protected boolean isViolation(Type originalType, Type castType) {
    return originalType.is(castType);
  }
}
