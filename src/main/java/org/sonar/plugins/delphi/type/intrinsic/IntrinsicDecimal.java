package org.sonar.plugins.delphi.type.intrinsic;

import org.sonar.plugins.delphi.type.Type.DecimalType;

public enum IntrinsicDecimal {
  SINGLE("Single", 4),
  DOUBLE("Double", 8),
  REAL("Real", 8),
  EXTENDED("Extended", 10),
  REAL48("Real48", 6),
  COMP("Comp", 8),
  CURRENCY("Currency", 8);

  public final DecimalType type;

  IntrinsicDecimal(String image, int size) {
    this.type = new DelphiDecimalType(image, size);
  }
}
