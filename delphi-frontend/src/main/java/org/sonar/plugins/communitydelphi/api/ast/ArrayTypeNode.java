package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ArrayTypeNode extends TypeNode {
  @Nonnull
  TypeNode getElementTypeNode();

  @Nullable
  ArrayIndicesNode getArrayIndices();
}
