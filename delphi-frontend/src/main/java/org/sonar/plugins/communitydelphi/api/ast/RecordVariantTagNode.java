package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.type.Typed;

public interface RecordVariantTagNode extends DelphiNode, Typed {
  NameDeclarationNode getTagName();

  TypeNode getTypeNode();
}
