package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;

public interface ArrayIndicesNode extends DelphiNode {
  List<TypeNode> getTypeNodes();
}
