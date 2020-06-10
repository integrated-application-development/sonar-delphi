package org.sonar.plugins.delphi.operator;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.sonar.plugins.delphi.symbol.declaration.parameter.Parameter;
import org.sonar.plugins.delphi.symbol.declaration.parameter.PointerMathParameter;
import org.sonar.plugins.delphi.symbol.resolve.Invocable;
import org.sonar.plugins.delphi.type.Type;

public final class PointerMathOperatorSignature implements Invocable {
  private final String name;
  private final List<Parameter> parameters;
  private final Type returnType;

  PointerMathOperatorSignature(String name, List<Type> parameterTypes, Type returnType) {
    this.name = name;
    this.parameters =
        parameterTypes.stream()
            .map(PointerMathParameter::create)
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
  public boolean isClassInvocable() {
    return true;
  }

  @Override
  public boolean isCallable() {
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PointerMathOperatorSignature that = (PointerMathOperatorSignature) o;
    return name.equals(that.name)
        && parameters.equals(that.parameters)
        && returnType.is(that.returnType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, parameters, returnType);
  }
}
