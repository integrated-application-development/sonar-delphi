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
package au.com.integradev.delphi.type.intrinsic;

import au.com.integradev.delphi.symbol.QualifiedNameImpl;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.List;
import org.sonar.plugins.communitydelphi.api.symbol.Qualifiable;
import org.sonar.plugins.communitydelphi.api.symbol.QualifiedName;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodKind;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public class IntrinsicMethod implements Qualifiable {
  private final QualifiedName qualifiedName;
  private final List<IntrinsicParameterData> parameters;
  private final Type returnType;
  private final boolean isVariadic;

  private IntrinsicMethod(
      String name, List<IntrinsicParameterData> parameters, Type returnType, boolean isVariadic) {
    this.qualifiedName = QualifiedNameImpl.of("System", name);
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
      this.returnType = TypeFactory.voidType();
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
