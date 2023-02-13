package au.com.integradev.delphi.antlr.ast.node;

import java.util.List;

public interface VarSectionNode extends DelphiNode {
  List<VarDeclarationNode> getDeclarations();
}
