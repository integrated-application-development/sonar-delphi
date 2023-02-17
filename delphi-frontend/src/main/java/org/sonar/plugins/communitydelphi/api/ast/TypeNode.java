package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;
import java.util.Set;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Typed;

public interface TypeNode extends DelphiNode, Typed {

  AncestorListNode getAncestorListNode();

  List<TypeReferenceNode> getParentTypeNodes();

  Set<Type> getParentTypes();
}
