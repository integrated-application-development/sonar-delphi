package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.symbol.declaration.MethodKind;
import au.com.integradev.delphi.type.Type;

public interface AnonymousMethodNode extends ExpressionNode {
  MethodParametersNode getMethodParametersNode();

  MethodReturnTypeNode getReturnTypeNode();

  Type getReturnType();

  MethodKind getMethodKind();

  boolean isFunction();
}
