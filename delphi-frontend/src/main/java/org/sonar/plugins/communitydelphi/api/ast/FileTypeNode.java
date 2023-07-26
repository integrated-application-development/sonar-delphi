package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nullable;

public interface FileTypeNode extends TypeNode {
  @Nullable
  TypeNode getTypeNode();
}
