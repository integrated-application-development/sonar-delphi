package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nullable;

public interface ProcedureTypeHeadingNode extends DelphiNode {
  @Nullable
  MethodParametersNode getMethodParametersNode();

  @Nullable
  MethodReturnTypeNode getMethodReturnTypeNode();

  boolean hasMethodParametersNode();
}
