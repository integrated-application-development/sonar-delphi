package org.sonar.plugins.delphi.type.parameter;

import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicMethod.IntrinsicParameterData;

public class IntrinsicParameter extends AbstractParameter {
  private IntrinsicParameter(
      Type type, boolean hasDefaultValue, boolean isOut, boolean isVar, boolean isConst) {
    super(type, hasDefaultValue, isOut, isVar, isConst);
  }

  public static Parameter create(IntrinsicParameterData data) {
    return new IntrinsicParameter(
        data.getType(), data.hasDefaultValue(), data.isOut(), data.isVar(), data.isConst());
  }

  public static Parameter create(Type type) {
    return new IntrinsicParameter(type, false, false, false, false);
  }

  @Override
  public String getImage() {
    return "_";
  }
}
