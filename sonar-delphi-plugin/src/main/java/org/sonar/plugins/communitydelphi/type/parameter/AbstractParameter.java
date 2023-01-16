/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.plugins.communitydelphi.type.parameter;

import com.google.common.collect.ComparisonChain;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.communitydelphi.type.Type;
import org.sonar.plugins.communitydelphi.type.generic.TypeSpecializationContext;

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
