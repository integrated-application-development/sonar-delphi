package org.sonar.plugins.communitydelphi.api.ast;

public interface ClassReferenceTypeNode extends TypeNode {
  TypeReferenceNode getClassOfTypeNode();
}
