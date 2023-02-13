package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Typed;
import java.util.List;
import java.util.Set;
import org.jetbrains.annotations.NotNull;

public interface TypeNode extends DelphiNode, Typed {

  AncestorListNode getAncestorListNode();

  List<TypeReferenceNode> getParentTypeNodes();

  Set<Type> getParentTypes();
}
