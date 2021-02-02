package org.sonar.plugins.delphi.operator;

import java.util.Set;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;

public interface Operator {
  Set<String> getNames();

  default boolean isOverloadedByMethod(MethodNameDeclaration method) {
    return method.getMethodKind() == MethodKind.OPERATOR && getNames().contains(method.getImage());
  }
}
