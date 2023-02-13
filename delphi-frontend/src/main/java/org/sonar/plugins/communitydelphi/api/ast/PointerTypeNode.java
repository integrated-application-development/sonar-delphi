package org.sonar.plugins.communitydelphi.api.ast;

public interface PointerTypeNode extends TypeNode {
  TypeNode getDereferencedTypeNode();
}
