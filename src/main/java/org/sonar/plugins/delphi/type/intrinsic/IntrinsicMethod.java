package org.sonar.plugins.delphi.type.intrinsic;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.sonar.plugins.delphi.symbol.Qualifiable;
import org.sonar.plugins.delphi.symbol.QualifiedName;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;

public class IntrinsicMethod implements Qualifiable {
  private final QualifiedName qualifiedName;
  private final List<IntrinsicParameterData> parameters;
  private final Type returnType;
  private final boolean isVariadic;

  private IntrinsicMethod(
      String name, List<IntrinsicParameterData> parameters, Type returnType, boolean isVariadic) {
    this.qualifiedName = QualifiedName.of("System", name);
    this.parameters = parameters;
    this.returnType = returnType;
    this.isVariadic = isVariadic;
  }

  static Builder builder(String name) {
    return new Builder(name);
  }

  @Override
  public QualifiedName getQualifiedName() {
    return qualifiedName;
  }

  public List<IntrinsicParameterData> getParameters() {
    return parameters;
  }

  public Type getReturnType() {
    return returnType;
  }

  public MethodKind getMethodKind() {
    return returnType.isVoid() ? MethodKind.PROCEDURE : MethodKind.FUNCTION;
  }

  public boolean isVariadic() {
    return isVariadic;
  }

  public static class IntrinsicParameterData {
    private final Type type;
    private final boolean hasDefaultValue;

    private IntrinsicParameterData(Type type, boolean hasDefaultValue) {
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

  static class Builder {
    private final String methodName;
    private List<Type> parameterTypes;
    private Type variadicParameter;
    private int requiredParameters;
    private Type returnType;

    private Builder(String methodName) {
      this.methodName = methodName;
      this.parameterTypes = Collections.emptyList();
      this.requiredParameters = -1;
      this.returnType = DelphiType.voidType();
    }

    @CanIgnoreReturnValue
    Builder parameters(Type... parameters) {
      this.parameterTypes = Arrays.asList(parameters);
      return this;
    }

    @CanIgnoreReturnValue
    Builder variadic(Type variadicType) {
      this.variadicParameter = variadicType;
      return this;
    }

    @CanIgnoreReturnValue
    Builder required(int requiredParameters) {
      this.requiredParameters = requiredParameters;
      return this;
    }

    @CanIgnoreReturnValue
    Builder returns(Type returnType) {
      this.returnType = returnType;
      return this;
    }

    IntrinsicMethod build() {
      return new IntrinsicMethod(
          methodName, buildParameters(), returnType, variadicParameter != null);
    }

    private List<IntrinsicParameterData> buildParameters() {
      List<IntrinsicParameterData> parameters = new ArrayList<>();
      for (int i = 0; i < parameterTypes.size(); ++i) {
        Type type = parameterTypes.get(i);
        boolean hasDefaultValue = requiredParameters != -1 && i >= requiredParameters;
        parameters.add(new IntrinsicParameterData(type, hasDefaultValue));
      }

      if (variadicParameter != null) {
        parameters.add(new IntrinsicParameterData(variadicParameter, true));
      }

      return parameters;
    }
  }
}
