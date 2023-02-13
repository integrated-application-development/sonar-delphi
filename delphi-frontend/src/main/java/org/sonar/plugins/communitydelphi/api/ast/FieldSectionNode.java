package org.sonar.plugins.communitydelphi.api.ast;

public interface FieldSectionNode extends DelphiNode, Visibility {
  boolean isClassFieldSection();
}
