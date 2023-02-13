package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.type.Typed;

public interface EnumElementNode extends DelphiNode, Typed {
  SimpleNameDeclarationNode getNameDeclarationNode();
}
