package org.sonar.plugins.delphi.symbol.declaration.parameter;

import com.google.common.collect.ComparisonChain;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

public abstract class AbstractParameter implements Parameter {
  @Override
  public String getImage() {
    return "_";
  }

  @Override
  public boolean isVar() {
    return false;
  }

  @Override
  public boolean isOut() {
    return false;
  }

  @Override
  public boolean isConst() {
    return false;
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
