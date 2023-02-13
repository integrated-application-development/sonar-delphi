package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;

public interface AncestorListNode extends DelphiNode {
  List<TypeReferenceNode> getParentTypeNodes();
}
