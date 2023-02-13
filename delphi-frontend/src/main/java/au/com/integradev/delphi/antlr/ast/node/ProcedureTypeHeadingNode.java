package au.com.integradev.delphi.antlr.ast.node;

import javax.annotation.Nullable;

public interface ProcedureTypeHeadingNode extends DelphiNode {
  @Nullable
  MethodParametersNode getMethodParametersNode();

  @Nullable
  MethodReturnTypeNode getMethodReturnTypeNode();

  boolean hasMethodParametersNode();
}
