package org.sonar.plugins.delphi.type.intrinsic;

import org.sonar.plugins.delphi.type.Type.DecimalType;

public enum IntrinsicDecimal {
  SINGLE("Single", 4),
  DOUBLE("Double", 8),
  REAL("Real", DOUBLE.type),
  EXTENDED("Extended", 10),
  REAL48("Real48", 6),
  COMP("Comp", 8),
  CURRENCY("Currency", 8);

  public final String image;
  public final DecimalType type;

  IntrinsicDecimal(String image, int size) {
    this.image = image;
    this.type = new DelphiDecimalType(image, size);
  }

  IntrinsicDecimal(String image, DecimalType aliased) {
    this.image = image;
    this.type = aliased;
  }
}
