package au.com.integradev.delphi.antlr.ast.node;

import javax.annotation.Nonnull;

public interface FileTypeNode extends TypeNode {
  @Nonnull
  default TypeNode getTypeNode() {
    return (TypeNode) jjtGetChild(1);
  }
}
