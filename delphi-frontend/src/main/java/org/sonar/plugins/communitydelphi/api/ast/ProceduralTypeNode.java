package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import au.com.integradev.delphi.type.Type;
import java.util.List;

public interface ProceduralTypeNode extends TypeNode {
  Type getReturnType();

  List<FormalParameterData> getParameters();
}
