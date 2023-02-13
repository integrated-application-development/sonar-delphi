package org.sonar.plugins.communitydelphi.api.ast;

import java.util.List;

public interface NameDeclarationListNode extends DelphiNode {
  List<NameDeclarationNode> getDeclarations();
}
