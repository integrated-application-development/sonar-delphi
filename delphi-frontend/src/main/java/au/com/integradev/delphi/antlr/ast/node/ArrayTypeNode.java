package au.com.integradev.delphi.antlr.ast.node;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ArrayTypeNode extends TypeNode {
  @Nonnull
  TypeNode getElementTypeNode();

  @Nullable
  ArrayIndicesNode getArrayIndices();
}
