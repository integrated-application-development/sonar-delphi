package au.com.integradev.delphi.antlr.ast.node;

public interface PointerTypeNode extends TypeNode {
  TypeNode getDereferencedTypeNode();
}
