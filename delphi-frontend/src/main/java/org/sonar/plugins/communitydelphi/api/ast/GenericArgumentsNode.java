package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;

public interface GenericArgumentsNode extends DelphiNode {
  List<TypeNode> getTypeArguments();
}
