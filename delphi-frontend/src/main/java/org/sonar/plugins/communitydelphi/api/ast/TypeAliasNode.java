package org.sonar.plugins.communitydelphi.api.ast;

public interface TypeAliasNode extends TypeNode {
  TypeReferenceNode getAliasedTypeNode();
}
