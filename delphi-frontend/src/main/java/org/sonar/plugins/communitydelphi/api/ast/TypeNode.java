package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Typed;
import java.util.List;
import java.util.Set;

public interface TypeNode extends DelphiNode, Typed {

  AncestorListNode getAncestorListNode();

  List<TypeReferenceNode> getParentTypeNodes();

  Set<Type> getParentTypes();
}
