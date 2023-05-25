package org.sonar.plugins.communitydelphi.api.ast;

public interface InterfaceTypeNode extends StructTypeNode {
  boolean isForwardDeclaration();

  InterfaceGuidNode getGuid();
}
