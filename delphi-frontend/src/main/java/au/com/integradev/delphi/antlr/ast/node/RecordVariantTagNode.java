package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.type.Typed;

public interface RecordVariantTagNode extends DelphiNode, Typed {
  NameDeclarationNode getTagName();

  TypeNode getTypeNode();
}
