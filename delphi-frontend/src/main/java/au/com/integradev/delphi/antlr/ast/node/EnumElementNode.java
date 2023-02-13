package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.type.Typed;

public interface EnumElementNode extends DelphiNode, Typed {
  SimpleNameDeclarationNode getNameDeclarationNode();
}
