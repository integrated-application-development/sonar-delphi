package org.sonar.plugins.communitydelphi.api.ast;

public interface ConstSectionNode extends DelphiNode, Visibility {
  @Override
  VisibilityType getVisibility();
}
