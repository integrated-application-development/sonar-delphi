package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import org.sonar.plugins.communitydelphi.api.type.Type;

public interface MethodParametersNode extends DelphiNode {
  List<FormalParameterData> getParameters();

  List<Type> getParameterTypes();

  FormalParameterListNode getFormalParametersList();

  boolean isEmpty();
}
