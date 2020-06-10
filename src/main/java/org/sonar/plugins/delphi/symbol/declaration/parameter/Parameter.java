package org.sonar.plugins.delphi.symbol.declaration.parameter;

import com.google.errorprone.annotations.Immutable;
import org.sonar.plugins.delphi.type.Typed;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

public interface Parameter extends Typed, Comparable<Parameter> {
  String getImage();

  boolean hasDefaultValue();

  boolean isOut();

  boolean isVar();

  boolean isConst();

  Parameter specialize(TypeSpecializationContext context);

  @Immutable
  interface ImmutableParameter extends Parameter {}
}
