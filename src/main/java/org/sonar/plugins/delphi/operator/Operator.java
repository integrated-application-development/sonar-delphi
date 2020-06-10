package org.sonar.plugins.delphi.operator;

import java.util.Set;
import org.sonar.plugins.delphi.symbol.declaration.MethodKind;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;

public interface Operator {
  OperatorData getData();

  default Set<IntrinsicOperatorSignature> getIntrinsicOperatorSignatures() {
    return getData().getIntrinsicOperatorSignatures();
  }

  default boolean isOverloadedByMethod(MethodNameDeclaration method) {
    return method.getMethodKind() == MethodKind.OPERATOR
        && getData().getOperatorNames().contains(method.getImage());
  }
}
