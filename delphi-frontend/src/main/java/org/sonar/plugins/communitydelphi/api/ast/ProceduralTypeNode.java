package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.type.Type;
import java.util.List;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;

public interface ProceduralTypeNode extends TypeNode {
  Type getReturnType();

  List<FormalParameterData> getParameters();
}
