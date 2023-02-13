package au.com.integradev.delphi.antlr.ast.node;

public interface TypeAliasNode extends TypeNode {
  TypeReferenceNode getAliasedTypeNode();
}
