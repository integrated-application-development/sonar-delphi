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
package org.sonar.plugins.communitydelphi.operator;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.symbol.resolve.Invocable;
import org.sonar.plugins.communitydelphi.type.Type;
import org.sonar.plugins.communitydelphi.type.parameter.IntrinsicParameter;
import org.sonar.plugins.communitydelphi.type.parameter.Parameter;

public final class OperatorIntrinsic implements Invocable {
  private final String name;
  private final List<Parameter> parameters;
  private final Type returnType;

  OperatorIntrinsic(String name, List<Type> parameterTypes, Type returnType) {
    this.name = name;
    this.parameters =
        parameterTypes.stream()
            .map(IntrinsicParameter::create)
            .collect(Collectors.toUnmodifiableList());
    this.returnType = returnType;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<Parameter> getParameters() {
    return parameters;
  }

  @Override
  public Type getReturnType() {
    return returnType;
  }

  @Override
  public boolean isCallable() {
    return false;
  }

  @Override
  public boolean isClassInvocable() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperatorIntrinsic that = (OperatorIntrinsic) o;
    return name.equals(that.name)
        && parameters.equals(that.parameters)
        && returnType.is(that.returnType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, parameters, returnType.getImage().toLowerCase());
  }
}
