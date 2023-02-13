package org.sonar.plugins.communitydelphi.api.ast;

public interface FileHeaderNode extends DelphiNode {
  QualifiedNameDeclarationNode getNameNode();

  String getName();

  String getNamespace();
}
