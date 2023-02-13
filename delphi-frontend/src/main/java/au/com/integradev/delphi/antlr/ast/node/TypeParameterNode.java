package au.com.integradev.delphi.antlr.ast.node;

import java.util.List;

public interface TypeParameterNode extends DelphiNode {
  List<NameDeclarationNode> getTypeParameterNameNodes();

  List<TypeReferenceNode> getTypeConstraintNodes();
}
