package au.com.integradev.delphi.antlr.ast.node;

import java.util.List;

public interface ArrayIndicesNode extends DelphiNode {
  List<TypeNode> getTypeNodes();
}
