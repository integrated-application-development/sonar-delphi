package org.sonar.plugins.delphi.operator;

import com.google.common.collect.ImmutableList;
import com.google.errorprone.annotations.Immutable;
import java.util.List;
import org.sonar.plugins.delphi.symbol.declaration.parameter.IntrinsicParameter;
import org.sonar.plugins.delphi.symbol.declaration.parameter.Parameter;
import org.sonar.plugins.delphi.symbol.declaration.parameter.Parameter.ImmutableParameter;
import org.sonar.plugins.delphi.symbol.resolve.Invocable;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.ImmutableType;

@Immutable
public final class IntrinsicOperatorSignature implements Invocable {
  private final String name;
  private final ImmutableList<ImmutableParameter> parameters;
  private final ImmutableType returnType;

  IntrinsicOperatorSignature(
      String name, List<ImmutableType> parameterTypes, ImmutableType returnType) {
    this.name = name;
    this.parameters =
        parameterTypes.stream()
            .map(IntrinsicParameter::create)
            .collect(ImmutableList.toImmutableList());
    this.returnType = returnType;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public List<Parameter> getParameters() {
    return ImmutableList.copyOf(parameters);
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
}
