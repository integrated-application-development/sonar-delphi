package org.sonar.plugins.delphi.type.intrinsic;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.type.DelphiProceduralType;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ImmutableType;
import org.sonar.plugins.delphi.type.Type.ProceduralType;

@Immutable
public class IntrinsicMethodData {
  private final String methodName;
  private final ImmutableList<IntrinsicMethodParameterData> parameters;
  private final ImmutableType returnType;
  private final boolean isVariadic;

  private IntrinsicMethodData(
      String methodName,
      ImmutableList<IntrinsicMethodParameterData> parameters,
      ImmutableType returnType,
      boolean isVariadic) {
    this.methodName = methodName;
    this.parameters = parameters;
    this.returnType = returnType;
    this.isVariadic = isVariadic;
  }

  static IntrinsicMethodDataBuilder builder(String name) {
    return new IntrinsicMethodDataBuilder(name);
  }

  public String getMethodName() {
    return methodName;
  }

  public List<IntrinsicMethodParameterData> getParameters() {
    return parameters;
  }

  public Type getReturnType() {
    return returnType;
  }

  public MethodKind getMethodKind() {
    return returnType.isVoid() ? MethodKind.PROCEDURE : MethodKind.FUNCTION;
  }

  public ProceduralType createMethodType() {
    return DelphiProceduralType.method(
        parameters.stream()
            .map(IntrinsicMethodParameterData::getType)
            .collect(Collectors.toUnmodifiableList()),
        returnType);
  }

  public boolean isVariadic() {
    return isVariadic;
  }

  @Immutable
  public static class IntrinsicMethodParameterData {
    private final ImmutableType type;
    private final boolean hasDefaultValue;

    private IntrinsicMethodParameterData(ImmutableType type, boolean hasDefaultValue) {
      this.type = type;
      this.hasDefaultValue = hasDefaultValue;
    }

    public Type getType() {
      return type;
    }

    public boolean hasDefaultValue() {
      return hasDefaultValue;
    }
  }

  static class IntrinsicMethodDataBuilder {
    private final String methodName;
    private List<ImmutableType> parameterTypes;
    private int requiredParameters;
    private ImmutableType returnType;
    private ImmutableType variadicParameter;

    private IntrinsicMethodDataBuilder(String methodName) {
      this.methodName = methodName;
      this.parameterTypes = Collections.emptyList();
      this.requiredParameters = -1;
      this.returnType = DelphiType.voidType();
    }

    IntrinsicMethodDataBuilder required(int requiredParameters) {
      this.requiredParameters = requiredParameters;
      return this;
    }

    IntrinsicMethodDataBuilder parameters(ImmutableType... parameters) {
      this.parameterTypes = Arrays.asList(parameters);
      return this;
    }

    IntrinsicMethodDataBuilder returns(ImmutableType returnType) {
      this.returnType = returnType;
      return this;
    }

    IntrinsicMethodDataBuilder variadic(ImmutableType variadicType) {
      this.variadicParameter = variadicType;
      return this;
    }

    IntrinsicMethodData build() {
      return new IntrinsicMethodData(
          methodName, buildParameters(), returnType, variadicParameter != null);
    }

    private ImmutableList<IntrinsicMethodParameterData> buildParameters() {
      ImmutableList.Builder<IntrinsicMethodParameterData> parameters = ImmutableList.builder();
      for (int i = 0; i < parameterTypes.size(); ++i) {
        ImmutableType type = parameterTypes.get(i);
        boolean hasDefaultValue = requiredParameters != -1 && i >= requiredParameters;
        parameters.add(new IntrinsicMethodParameterData(type, hasDefaultValue));
      }

      if (variadicParameter != null) {
        parameters.add(new IntrinsicMethodParameterData(variadicParameter, true));
      }

      return parameters.build();
    }
  }
}
