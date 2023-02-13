package au.com.integradev.delphi.antlr.ast.node;

import java.util.List;

public interface NameDeclarationListNode extends DelphiNode {
  List<NameDeclarationNode> getDeclarations();
}
