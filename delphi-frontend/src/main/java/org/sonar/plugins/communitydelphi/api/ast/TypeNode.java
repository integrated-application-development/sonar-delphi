package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Typed;
import java.util.List;
import java.util.Set;

public interface TypeNode extends DelphiNode, Typed {

  AncestorListNode getAncestorListNode();

  List<TypeReferenceNode> getParentTypeNodes();

  Set<Type> getParentTypes();
}
