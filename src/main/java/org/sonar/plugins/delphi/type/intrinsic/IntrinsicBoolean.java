package org.sonar.plugins.delphi.type.intrinsic;

import org.sonar.plugins.delphi.type.Type.BooleanType;

public enum IntrinsicBoolean {
  BOOLEAN("Boolean", 1),
  BYTEBOOL("ByteBool", 1),
  WORDBOOL("WordBool", 2),
  LONGBOOL("LongBool", 4);

  public final BooleanType type;

  IntrinsicBoolean(String image, int size) {
    this.type = new DelphiBooleanType(image, size);
  }
}
