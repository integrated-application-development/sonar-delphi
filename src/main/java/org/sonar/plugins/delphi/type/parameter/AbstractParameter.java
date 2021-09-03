package org.sonar.plugins.delphi.type.parameter;

import com.google.common.collect.ComparisonChain;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

public abstract class AbstractParameter implements Parameter {
  private final Type type;
  private final boolean hasDefaultValue;
  private final boolean isOut;
  private final boolean isVar;
  private final boolean isConst;

  protected AbstractParameter(
      Type type, boolean hasDefaultValue, boolean isOut, boolean isVar, boolean isConst) {
    this.type = type;
    this.hasDefaultValue = hasDefaultValue;
    this.isOut = isOut;
    this.isVar = isVar;
    this.isConst = isConst;
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }

  @Override
  public boolean hasDefaultValue() {
    return hasDefaultValue;
  }

  @Override
  public boolean isOut() {
    return isOut;
  }

  @Override
  public boolean isVar() {
    return isVar;
  }

  @Override
  public boolean isConst() {
    return isConst;
  }

  @Override
  public Parameter specialize(TypeSpecializationContext context) {
    return this;
  }

  @Override
  public final boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AbstractParameter)) {
      return false;
    }
    Parameter that = (Parameter) o;
    return getImage().equalsIgnoreCase(that.getImage()) && getType().is(that.getType());
  }

  @Override
  public final int hashCode() {
    return Objects.hash(getImage().toLowerCase(), getType().getImage());
  }

  @Override
  public final int compareTo(@NotNull Parameter other) {
    return ComparisonChain.start()
        .compare(getImage(), other.getImage(), String.CASE_INSENSITIVE_ORDER)
        .compare(getType().getImage(), other.getType().getImage(), String.CASE_INSENSITIVE_ORDER)
        .result();
  }
}
