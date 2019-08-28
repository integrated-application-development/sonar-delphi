package org.sonar.plugins.delphi.symbol.resolve;

import static java.util.function.Predicate.not;

import java.util.List;
import org.sonar.plugins.delphi.symbol.ParameterDeclaration;
import org.sonar.plugins.delphi.type.Type;

public interface Invocable {
  List<ParameterDeclaration> getParameters();

  Type getReturnType();

  boolean isCallable();

  boolean isClassInvocable();

  default int getParametersCount() {
    return getParameters().size();
  }

  default int getRequiredParametersCount() {
    return (int)
        getParameters().stream().filter(not(ParameterDeclaration::hasDefaultValue)).count();
  }

  default ParameterDeclaration getParameter(int index) {
    return getParameters().get(index);
  }
}
