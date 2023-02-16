package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.type.Typed;

public interface EnumElementNode extends DelphiNode, Typed {
  SimpleNameDeclarationNode getNameDeclarationNode();
}
