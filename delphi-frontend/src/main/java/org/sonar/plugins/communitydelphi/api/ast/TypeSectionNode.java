package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;

public interface TypeSectionNode extends DelphiNode {
  List<TypeDeclarationNode> getDeclarations();
}
