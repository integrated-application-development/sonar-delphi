package org.sonar.plugins.communitydelphi.api.ast;

public interface MethodDeclarationNode extends MethodNode, Visibility {
  boolean isOverride();

  boolean isVirtual();

  boolean isMessage();
}
