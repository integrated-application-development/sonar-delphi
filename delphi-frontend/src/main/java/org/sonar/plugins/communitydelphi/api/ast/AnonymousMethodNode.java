package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodKind;

public interface AnonymousMethodNode extends ExpressionNode {
  MethodParametersNode getMethodParametersNode();

  MethodReturnTypeNode getReturnTypeNode();

  Type getReturnType();

  MethodKind getMethodKind();

  boolean isFunction();
}
