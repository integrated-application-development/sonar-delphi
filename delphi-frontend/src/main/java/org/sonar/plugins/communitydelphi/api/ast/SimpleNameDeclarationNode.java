package org.sonar.plugins.communitydelphi.api.ast;

public interface SimpleNameDeclarationNode extends NameDeclarationNode {
  IdentifierNode getIdentifier();
}
