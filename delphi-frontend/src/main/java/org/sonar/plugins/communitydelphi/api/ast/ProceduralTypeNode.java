package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.type.Type;

public interface ProceduralTypeNode extends TypeNode {
  Type getReturnType();

  List<FormalParameterData> getParameters();
}
