package au.com.integradev.delphi.antlr.ast.node;

public interface MethodResolutionClauseNode extends DelphiNode {
  NameReferenceNode getInterfaceMethodNameNode();

  NameReferenceNode getImplementationMethodNameNode();
}
