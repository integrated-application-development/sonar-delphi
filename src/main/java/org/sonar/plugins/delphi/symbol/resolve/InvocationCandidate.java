package org.sonar.plugins.delphi.symbol.resolve;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.operator.OperatorIntrinsic;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.parameter.Parameter;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;
import org.sonar.plugins.delphi.type.intrinsic.IntrinsicType;

/**
 * Stores information about an invocation candidate, used for overload resolution. Based directly
 * off of the tcandidate record from the FreePascal compiler
 *
 * @see <a href="https://github.com/graemeg/freepascal/blob/master/compiler/htypechk.pas#L50">
 *     tcandidate</a>
 */
public final class InvocationCandidate {
  public static final int CONVERT_LEVELS = 8;

  private final Invocable data;
  private int exactCount;
  private int equalCount;
  private final int[] convertLevelCount;
  private int convertOperatorCount;
  private double ordinalDistance;
  private int signMismatchCount;
  private int structMismatchCount;
  private int proceduralDistance;
  private final List<VariantConversionType> variantConversions;
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

  public int getConvertOperatorCount() {
    return convertOperatorCount;
  }

  public void incrementConvertOperatorCount() {
    ++this.convertOperatorCount;
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

  public void addVariantConversion(VariantConversionType variantConversionType) {
    variantConversions.add(variantConversionType);
  }

  public VariantConversionType getVariantConversionType(int argumentIndex) {
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
      NameDeclaration declaration, List<Type> argumentTypes) {
    if (!(declaration instanceof MethodNameDeclaration)) {
      return null;
    }

    MethodNameDeclaration methodDeclaration = (MethodNameDeclaration) declaration;
    if (!methodDeclaration.isGeneric()) {
      return null;
    }

    Map<Type, Type> argumentsByTypeParameters = new HashMap<>();
    for (int i = 0; i < argumentTypes.size(); ++i) {
      Type parameterType = methodDeclaration.getParameter(i).getType();
      if (!parameterType.isTypeParameter()) {
        continue;
      }

      Type argumentType = argumentTypes.get(i);
      Type existingMapping = argumentsByTypeParameters.get(parameterType);

      if (existingMapping != null && !existingMapping.is(argumentType)) {
        // Can't implicitly specialize the declaration with these arguments
        return null;
      }

      argumentsByTypeParameters.put(parameterType, argumentType);
    }

    if (argumentsByTypeParameters.size() != methodDeclaration.getTypeParameters().size()) {
      return null;
    }

    List<Type> typeArguments;

    typeArguments =
        methodDeclaration.getTypeParameters().stream()
            .map(TypedDeclaration::getType)
            .map(argumentsByTypeParameters::get)
            .collect(Collectors.toUnmodifiableList());

    var context = new TypeSpecializationContext(methodDeclaration, typeArguments);
    Invocable invocable = (Invocable) methodDeclaration.specialize(context);

    return new InvocationCandidate(invocable);
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
