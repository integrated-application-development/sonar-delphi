package au.com.integradev.delphi.antlr.ast.node;

import au.com.integradev.delphi.type.Type;
import javax.annotation.Nonnull;

public interface HelperTypeNode extends StructTypeNode {
  @Nonnull
  TypeNode getFor();
}
