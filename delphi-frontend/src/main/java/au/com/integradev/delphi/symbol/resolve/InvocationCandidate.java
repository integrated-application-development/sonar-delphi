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
package au.com.integradev.delphi.symbol.resolve;

import au.com.integradev.delphi.operator.OperatorIntrinsic;
import au.com.integradev.delphi.type.generic.TypeSpecializationContextImpl;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.sonar.plugins.communitydelphi.api.symbol.Invocable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.communitydelphi.api.type.IntrinsicType;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.ArrayConstructorType;
import org.sonar.plugins.communitydelphi.api.type.Type.CollectionType;

/**
 * Stores information about an invocation candidate, used for overload resolution. Based directly
 * off of the tcandidate record from the FreePascal compiler
 *
 * @see <a href="https://github.com/fpc/FPCSource/blob/main/compiler/htypechk.pas#L50">
 *     tcandidate</a>
 */
public final class InvocationCandidate {
  public static final int CONVERT_LEVELS = 8;

  private final Invocable data;
  private int exactCount;
  private int equalCount;
  private final int[] convertLevelCount;
  private int implicitConversionFromCount;
  private int implicitConversionToCount;
  private double ordinalDistance;
  private int signMismatchCount;
  private int numericMismatchCount;
  private int structMismatchCount;
  private int proceduralDistance;
  private int codePageDistance;
  private final List<Type> variantConversions;
  private boolean invalid;

  public InvocationCandidate(Invocable invocable) {
    this.data = invocable;
    this.convertLevelCount = new int[CONVERT_LEVELS];
    this.variantConversions = new ArrayList<>(invocable.getParametersCount());
  }

  public Invocable getData() {
    return data;
  }

  public int getExactCount() {
    return exactCount;
  }

  public void incrementExactCount() {
    ++this.exactCount;
  }

  public int getEqualCount() {
    return equalCount;
  }

  public void incrementEqualCount() {
    ++this.equalCount;
  }

  public int getConvertLevelCount(int convertLevel) {
    return convertLevelCount[convertLevel - 1];
  }

  public void incrementConvertLevelCount(int convertLevel) {
    ++this.convertLevelCount[convertLevel - 1];
  }

  public int getImplicitConversionFromCount() {
    return implicitConversionFromCount;
  }

  public void incrementImplicitConversionFromCount() {
    ++this.implicitConversionFromCount;
  }

  public int getImplicitConversionToCount() {
    return implicitConversionToCount;
  }

  public void incrementImplicitConversionToCount() {
    ++this.implicitConversionToCount;
  }

  public double getOrdinalDistance() {
    return ordinalDistance;
  }

  public void increaseOrdinalDistance(double ordinalDistance) {
    this.ordinalDistance += ordinalDistance;
  }

  public int getSignMismatchCount() {
    return signMismatchCount;
  }

  public void incrementSignMismatchCount() {
    ++this.signMismatchCount;
  }

  public int getNumericMismatchCount() {
    return numericMismatchCount;
  }

  public void incrementNumericMismatchCount() {
    ++this.numericMismatchCount;
  }

  public int getStructMismatchCount() {
    return structMismatchCount;
  }

  public void incrementStructMismatchCount() {
    ++this.structMismatchCount;
  }

  public void increaseProceduralDistance(int proceduralDistance) {
    this.proceduralDistance += proceduralDistance;
  }

  public int getProceduralDistance() {
    return proceduralDistance;
  }

  public void increaseCodePageDistance(int codePageDistance) {
    this.codePageDistance += codePageDistance;
  }

  public int getCodePageDistance() {
    return codePageDistance;
  }

  public void addVariantConversion(Type variantConversionType) {
    variantConversions.add(variantConversionType);
  }

  public Type getVariantConversionType(int argumentIndex) {
    return variantConversions.get(argumentIndex);
  }

  public boolean isOperatorIntrinsic() {
    return data instanceof OperatorIntrinsic;
  }

  public boolean isVariantOperator() {
    return isOperatorIntrinsic()
        && data.getParameters().stream()
            .map(Parameter::getType)
            .anyMatch(type -> type.is(IntrinsicType.VARIANT));
  }

  public boolean isInvalid() {
    return invalid;
  }

  public void setInvalid() {
    this.invalid = true;
  }

  public static InvocationCandidate implicitSpecialization(
      Invocable invocable, List<Type> argumentTypes) {
    if (!(invocable instanceof RoutineNameDeclaration)) {
      return null;
    }

    RoutineNameDeclaration routineDeclaration = (RoutineNameDeclaration) invocable;
    if (!routineDeclaration.isGeneric()) {
      return null;
    }

    Map<Type, Type> argumentsByTypeParameters = new HashMap<>();
    for (int i = 0; i < argumentTypes.size(); ++i) {
      Type parameterType = routineDeclaration.getParameter(i).getType();
      Type argumentType = argumentTypes.get(i);

      if (parameterType.isOpenArray()) {
        Type argumentElementType = getArrayElementType(argumentType);
        if (argumentElementType != null) {
          parameterType = ((CollectionType) parameterType).elementType();
          argumentType = argumentElementType;
        }
      }

      if (!parameterType.isTypeParameter()) {
        continue;
      }

      Type existingMapping = argumentsByTypeParameters.get(parameterType);

      if (existingMapping != null && !existingMapping.is(argumentType)) {
        // Can't implicitly specialize the declaration with these arguments
        return null;
      }

      argumentsByTypeParameters.put(parameterType, argumentType);
    }

    if (argumentsByTypeParameters.size() != routineDeclaration.getTypeParameters().size()) {
      return null;
    }

    List<Type> typeArguments;

    typeArguments =
        routineDeclaration.getTypeParameters().stream()
            .map(TypedDeclaration::getType)
            .map(argumentsByTypeParameters::get)
            .collect(Collectors.toUnmodifiableList());

    var context = new TypeSpecializationContextImpl(routineDeclaration, typeArguments);
    Invocable specialized = (Invocable) routineDeclaration.specialize(context);

    return new InvocationCandidate(specialized);
  }

  private static Type getArrayElementType(Type type) {
    if (type.isArray()) {
      return ((CollectionType) type).elementType();
    } else if (type.isArrayConstructor()) {
      return getHomogeneousElementType((ArrayConstructorType) type);
    } else {
      return null;
    }
  }

  private static Type getHomogeneousElementType(ArrayConstructorType type) {
    List<Type> elementTypes = type.elementTypes();
    if (elementTypes.isEmpty()) {
      return null;
    }

    Type result = elementTypes.get(0);
    for (Type elementType : elementTypes) {
      if (!elementType.is(result)) {
        result = null;
        break;
      }
    }

    return result;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    InvocationCandidate candidate = (InvocationCandidate) o;
    return data.equals(candidate.data);
  }

  @Override
  public int hashCode() {
    return Objects.hash(data);
  }
}
