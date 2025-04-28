/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.type.generic;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.GenerifiableDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.TypeParameterType;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

public final class TypeSpecializationContextImpl implements TypeSpecializationContext {
  private static final Comparator<Type> COMPARATOR = Comparator.comparing(Type::getImage);
  private final Map<Type, Type> argumentsByParameter;

  public TypeSpecializationContextImpl(NameDeclaration declaration, List<Type> typeArguments) {
    argumentsByParameter = new TreeMap<>(COMPARATOR);

    if (!(declaration instanceof GenerifiableDeclaration)) {
      return;
    }

    List<Type> typeParameters =
        ((GenerifiableDeclaration) declaration)
            .getTypeParameters().stream()
                .map(TypedDeclaration::getType)
                .collect(Collectors.toList());

    if (typeParameters.size() != typeArguments.size()) {
      return;
    }

    for (int i = 0; i < typeParameters.size(); ++i) {
      Type parameter = typeParameters.get(i);
      Type argument = typeArguments.get(i);

      if (constraintViolated(parameter, argument)) {
        argumentsByParameter.clear();
        break;
      }

      argumentsByParameter.put(parameter, argument);
    }
  }

  private static boolean constraintViolated(Type parameter, Type argument) {
    return parameter.isTypeParameter()
        && ((TypeParameterType) parameter)
            .constraintItems().stream()
                .map(constraint -> constraint.satisfiedBy(argument))
                .anyMatch(s -> !s);
  }

  @Override
  @Nullable
  public Type getArgument(Type parameter) {
    return argumentsByParameter.get(parameter);
  }

  @Override
  public boolean hasSignatureMismatch() {
    return argumentsByParameter.isEmpty();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TypeSpecializationContextImpl that = (TypeSpecializationContextImpl) o;
    return argumentsByParameter.equals(that.argumentsByParameter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(argumentsByParameter);
  }
}
