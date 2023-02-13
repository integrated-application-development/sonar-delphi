package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;

public interface VarSectionNode extends DelphiNode {
  List<VarDeclarationNode> getDeclarations();
}
