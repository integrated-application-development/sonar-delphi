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
package au.com.integradev.delphi.type.factory;

import au.com.integradev.delphi.type.generic.GenerifiableTypeImpl;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ProceduralType;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

public final class ProceduralTypeImpl extends GenerifiableTypeImpl implements ProceduralType {
  private final int size;
  private final ProceduralKind kind;
  private final List<Parameter> parameters;
  private final Type returnType;
  private final boolean variadic;

  ProceduralTypeImpl(
      int size,
      ProceduralKind kind,
      List<Parameter> parameters,
      Type returnType,
      boolean variadic) {
    this.size = size;
    this.kind = kind;
    this.parameters = List.copyOf(parameters);
    this.returnType = returnType;
    this.variadic = variadic;
  }

  @Override
  public String getImage() {
    return kind.name() + makeSignature(parameters, returnType);
  }

  @Override
  public int size() {
    return size;
  }

  private static String makeSignature(Parameter parameter) {
    String signature = "";

    if (parameter.isOut()) {
      signature += "out ";
    }

    if (parameter.isVar()) {
      signature += "var ";
    }

    if (parameter.isConst()) {
      signature += "const ";
    }

    signature += parameter.getImage() + ": " + parameter.getType().getImage();

    return signature;
  }

  private static String makeSignature(List<Parameter> parameters, Type returnType) {
    return "("
        + parameters.stream()
            .map(ProceduralTypeImpl::makeSignature)
            .collect(Collectors.joining("; "))
        + "): "
        + returnType.getImage();
  }

  @Override
  public List<Parameter> parameters() {
    return parameters;
  }

  @Override
  public int parametersCount() {
    return variadic ? 255 : parameters().size();
  }

  @Override
  public Parameter getParameter(int index) {
    if (index < parameters().size()) {
      return parameters().get(index);
    } else if (variadic) {
      return Iterables.getLast(parameters());
    }

    throw new IndexOutOfBoundsException(
        "Invalid parameter declaration access (Size:"
            + parameters().size()
            + " Index:"
            + index
            + ")");
  }

  @Override
  public Type returnType() {
    return returnType;
  }

  @Override
  public ProceduralKind kind() {
    return kind;
  }

  @Override
  public boolean isProcedural() {
    return true;
  }

  @Override
  public boolean isRoutine() {
    return kind == ProceduralKind.ROUTINE;
  }

  @Override
  public boolean canBeSpecialized(TypeSpecializationContext context) {
    if (returnType.canBeSpecialized(context)) {
      return true;
    }
    return parameters.stream()
        .map(Parameter::getType)
        .anyMatch(type -> type.canBeSpecialized(context));
  }

  @Override
  public GenerifiableTypeImpl doSpecialization(TypeSpecializationContext context) {
    return new ProceduralTypeImpl(
        size,
        kind,
        parameters.stream()
            .map(parameter -> parameter.specialize(context))
            .collect(Collectors.toUnmodifiableList()),
        returnType.specialize(context),
        variadic);
  }
}
