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
package au.com.integradev.delphi.type.intrinsic;

import au.com.integradev.delphi.symbol.QualifiedNameImpl;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.ArrayList;
import java.util.List;
import org.sonar.plugins.communitydelphi.api.symbol.Qualifiable;
import org.sonar.plugins.communitydelphi.api.symbol.QualifiedName;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineKind;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypedDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeFactory;

public final class IntrinsicRoutine implements Qualifiable {
  private final QualifiedName qualifiedName;
  private final List<IntrinsicParameterData> parameters;
  private final Type returnType;
  private final boolean isVariadic;

  private IntrinsicRoutine(
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

  public RoutineKind getRoutineKind() {
    return returnType.isVoid() ? RoutineKind.PROCEDURE : RoutineKind.FUNCTION;
  }

  public boolean isVariadic() {
    return isVariadic;
  }

  public static final class IntrinsicParameterData {
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

    static final class Builder {
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

      IntrinsicParameterData build(DelphiScope scope) {
        Type resolvedType = resolveType(type, scope);
        return new IntrinsicParameterData(resolvedType, isOut, isVar, isConst, hasDefaultValue);
      }
    }
  }

  static final class Builder {
    private final String routineName;
    private final List<IntrinsicParameterData.Builder> parameters;
    private IntrinsicParameterData.Builder variadicParameter;
    private int requiredParameters;
    private Type returnType;

    private Builder(String routineName) {
      this.routineName = routineName;
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

    IntrinsicRoutine build(DelphiScope scope) {
      return new IntrinsicRoutine(
          routineName, buildParameters(scope), buildReturnType(scope), variadicParameter != null);
    }

    private List<IntrinsicParameterData> buildParameters(DelphiScope scope) {
      List<IntrinsicParameterData> result = new ArrayList<>();

      for (int i = 0; i < parameters.size(); ++i) {
        IntrinsicParameterData.Builder paramBuilder = parameters.get(i);
        paramBuilder.hasDefaultValue(requiredParameters != -1 && i >= requiredParameters);
        result.add(paramBuilder.build(scope));
      }

      if (variadicParameter != null) {
        variadicParameter.hasDefaultValue(true);
        result.add(variadicParameter.build(scope));
      }

      return result;
    }

    private Type buildReturnType(DelphiScope scope) {
      return resolveType(returnType, scope);
    }
  }

  private static Type resolveType(Type type, DelphiScope scope) {
    if (type.isUnresolved()) {
      String simpleName = type.getImage();
      type =
          scope.getTypeDeclarations().stream()
              .filter(declaration -> declaration.getName().equalsIgnoreCase(simpleName))
              .map(TypedDeclaration::getType)
              .findFirst()
              .orElse(type);
    }
    return type;
  }
}
