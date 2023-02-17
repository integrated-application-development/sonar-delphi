package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodKind;
import org.sonar.plugins.communitydelphi.api.type.Type;

public interface AnonymousMethodNode extends ExpressionNode {
  MethodParametersNode getMethodParametersNode();

  MethodReturnTypeNode getReturnTypeNode();

  Type getReturnType();

  MethodKind getMethodKind();

  boolean isFunction();
}
