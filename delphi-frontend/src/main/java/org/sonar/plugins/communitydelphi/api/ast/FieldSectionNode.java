package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;

public interface FieldSectionNode extends DelphiNode, Visibility {
  boolean isClassFieldSection();

  List<FieldDeclarationNode> getDeclarations();
}
