package au.com.integradev.delphi.antlr.ast.node;

public interface FileHeaderNode extends DelphiNode {
  QualifiedNameDeclarationNode getNameNode();

  String getName();

  String getNamespace();
}
