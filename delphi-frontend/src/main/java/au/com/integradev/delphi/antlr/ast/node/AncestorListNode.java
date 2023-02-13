package au.com.integradev.delphi.antlr.ast.node;

import java.util.List;

public interface AncestorListNode extends DelphiNode {
  List<TypeReferenceNode> getParentTypeNodes();
}
