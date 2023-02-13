package org.sonar.plugins.communitydelphi.api.ast;

public interface MethodResolutionClauseNode extends DelphiNode {
  NameReferenceNode getInterfaceMethodNameNode();

  NameReferenceNode getImplementationMethodNameNode();
}
