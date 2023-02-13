package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.ast.FormalParameterNode.FormalParameterData;
import au.com.integradev.delphi.type.Type;
import java.util.List;

public interface FormalParameterListNode extends DelphiNode {
  List<FormalParameterData> getParameters();

  List<Type> getParameterTypes();
}
