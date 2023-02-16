package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.type.Typed;

public interface RecordVariantTagNode extends DelphiNode, Typed {
  NameDeclarationNode getTagName();

  TypeNode getTypeNode();
}
