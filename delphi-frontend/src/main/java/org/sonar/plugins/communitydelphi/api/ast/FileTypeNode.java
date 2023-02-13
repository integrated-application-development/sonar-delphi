package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nonnull;

public interface FileTypeNode extends TypeNode {
  @Nonnull
  default TypeNode getTypeNode() {
    return (TypeNode) jjtGetChild(1);
  }
}
