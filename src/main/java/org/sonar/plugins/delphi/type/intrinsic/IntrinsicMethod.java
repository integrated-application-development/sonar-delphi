package org.sonar.plugins.delphi.type.intrinsic;

import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
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
    private final boolean isOut;
    private final boolean isVar;
    private final boolean isConst;
    private final boolean hasDefaultValue;

    private IntrinsicParameterData(
        Type type, boolean isOut, boolean isVar, boolean isConst, boolean hasDefaultValue) {
      this.type = type;
      this.isOut = isOut;
      this.isVar = isVar;
      this.isConst = isConst;
      this.hasDefaultValue = hasDefaultValue;
    }

    public Type getType() {
      return type;
    }

    public boolean isOut() {
      return isOut;
    }

    public boolean isVar() {
      return isVar;
    }

    public boolean isConst() {
      return isConst;
    }

    public boolean hasDefaultValue() {
      return hasDefaultValue;
    }

    static class Builder {
      private final Type type;
      private final boolean isOut;
      private final boolean isVar;
      private final boolean isConst;
      private boolean hasDefaultValue;

      private Builder(Type type, boolean isOut, boolean isVar, boolean isConst) {
        this.type = type;
        this.isOut = isOut;
        this.isVar = isVar;
        this.isConst = isConst;
      }

      @CanIgnoreReturnValue
      Builder hasDefaultValue(boolean hasDefaultValue) {
        this.hasDefaultValue = hasDefaultValue;
        return this;
      }

      IntrinsicParameterData build() {
        return new IntrinsicParameterData(type, isOut, isVar, isConst, hasDefaultValue);
      }
    }
  }

  static class Builder {
    private final String methodName;
    private List<IntrinsicParameterData.Builder> parameters;
    private IntrinsicParameterData.Builder variadicParameter;
    private int requiredParameters;
    private Type returnType;

    private Builder(String methodName) {
      this.methodName = methodName;
      this.parameters = new ArrayList<>();
      this.requiredParameters = -1;
      this.returnType = DelphiType.voidType();
    }

    @CanIgnoreReturnValue
    Builder param(Type type) {
      this.parameters.add(new IntrinsicParameterData.Builder(type, false, false, false));
      return this;
    }

    @CanIgnoreReturnValue
    Builder outParam(Type type) {
      this.parameters.add(new IntrinsicParameterData.Builder(type, true, false, false));
      return this;
    }

    @CanIgnoreReturnValue
    Builder varParam(Type type) {
      this.parameters.add(new IntrinsicParameterData.Builder(type, false, true, false));
      return this;
    }

    @CanIgnoreReturnValue
    Builder constParam(Type type) {
      this.parameters.add(new IntrinsicParameterData.Builder(type, false, false, true));
      return this;
    }

    @CanIgnoreReturnValue
    Builder variadic(Type type) {
      this.variadicParameter = new IntrinsicParameterData.Builder(type, false, false, false);
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
      List<IntrinsicParameterData> result = new ArrayList<>();

      for (int i = 0; i < parameters.size(); ++i) {
        IntrinsicParameterData.Builder paramBuilder = parameters.get(i);
        paramBuilder.hasDefaultValue(requiredParameters != -1 && i >= requiredParameters);
        result.add(paramBuilder.build());
      }

      if (variadicParameter != null) {
        variadicParameter.hasDefaultValue(true);
        result.add(variadicParameter.build());
      }

      return result;
    }
  }
}
