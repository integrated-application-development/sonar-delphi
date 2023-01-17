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
package au.com.integradev.delphi.type.generic;

import au.com.integradev.delphi.symbol.declaration.GenerifiableDeclaration;
import au.com.integradev.delphi.symbol.declaration.TypedDeclaration;
import au.com.integradev.delphi.type.Type;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;

public final class TypeSpecializationContext {
  private static final Comparator<Type> COMPARATOR = Comparator.comparing(Type::getImage);
  private final Map<Type, Type> argumentsByParameter;

  public TypeSpecializationContext(NameDeclaration declaration, List<Type> typeArguments) {
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
      argumentsByParameter.put(parameter, argument);
    }
  }

  @Nullable
  public Type getArgument(Type parameter) {
    return argumentsByParameter.get(parameter);
  }

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
    TypeSpecializationContext that = (TypeSpecializationContext) o;
    return argumentsByParameter.equals(that.argumentsByParameter);
  }

  @Override
  public int hashCode() {
    return Objects.hash(argumentsByParameter);
  }
}
