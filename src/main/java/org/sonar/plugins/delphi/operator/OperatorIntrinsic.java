package org.sonar.plugins.delphi.operator;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.symbol.resolve.Invocable;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.parameter.IntrinsicParameter;
import org.sonar.plugins.delphi.type.parameter.Parameter;

public final class OperatorIntrinsic implements Invocable {
  private final String name;
  private final List<Parameter> parameters;
  private final Type returnType;

  OperatorIntrinsic(String name, List<Type> parameterTypes, Type returnType) {
    this.name = name;
    this.parameters =
        parameterTypes.stream()
            .map(IntrinsicParameter::create)
            .collect(Collectors.toUnmodifiableList());
    this.returnType = returnType;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<Parameter> getParameters() {
    return parameters;
  }

  @Override
  public Type getReturnType() {
    return returnType;
  }

  @Override
  public boolean isCallable() {
    return false;
  }

  @Override
  public boolean isClassInvocable() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperatorIntrinsic that = (OperatorIntrinsic) o;
    return name.equals(that.name)
        && parameters.equals(that.parameters)
        && returnType.is(that.returnType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, parameters, returnType.getImage().toLowerCase());
  }
}
