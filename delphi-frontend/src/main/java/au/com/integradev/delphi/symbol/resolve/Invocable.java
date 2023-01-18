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
package au.com.integradev.delphi.symbol.resolve;

import static java.util.function.Predicate.not;

import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.parameter.Parameter;
import java.util.List;

public interface Invocable {
  String getName();

  List<Parameter> getParameters();

  Type getReturnType();

  boolean isCallable();

  boolean isClassInvocable();

  default int getParametersCount() {
    return getParameters().size();
  }

  default int getRequiredParametersCount() {
    return (int) getParameters().stream().filter(not(Parameter::hasDefaultValue)).count();
  }

  default Parameter getParameter(int index) {
    return getParameters().get(index);
  }

  default boolean hasSameParameterTypes(Invocable other) {
    if (getParametersCount() != other.getParametersCount()) {
      return false;
    }

    for (int i = 0; i < getParametersCount(); ++i) {
      if (!getParameter(i).getType().is(other.getParameter(i).getType())) {
        return false;
      }
    }

    return true;
  }
}
