package org.sonar.plugins.delphi.antlr.ast.node;

import java.util.Collections;
import java.util.List;
import org.antlr.runtime.Token;
import org.sonar.plugins.delphi.antlr.ast.node.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.delphi.type.DelphiType;
import org.sonar.plugins.delphi.type.Type;

public abstract class ProceduralTypeNode extends TypeNode {
  protected ProceduralTypeNode(Token token) {
    super(token);
  }

  protected ProceduralTypeNode(int tokenType) {
    super(tokenType);
  }

  private ProcedureTypeHeadingNode getHeading() {
    return (ProcedureTypeHeadingNode) jjtGetChild(0);
  }

  public Type getReturnType() {
    MethodReturnTypeNode returnTypeNode = getHeading().getMethodReturnTypeNode();
    return returnTypeNode == null ? DelphiType.voidType() : returnTypeNode.getTypeNode().getType();
  }

  public List<FormalParameterData> getParameters() {
    MethodParametersNode parametersNode = getHeading().getMethodParametersNode();
    return parametersNode == null ? Collections.emptyList() : parametersNode.getParameters();
  }
}
