package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.type.Type;
import java.util.List;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;

public interface FormalParameterListNode extends DelphiNode {
  List<FormalParameterData> getParameters();

  List<Type> getParameterTypes();
}
