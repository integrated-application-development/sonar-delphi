package org.sonar.plugins.delphi.antlr.ast.node;

import javax.annotation.Nullable;
import org.antlr.runtime.Token;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.visitors.DelphiParserVisitor;
import org.sonar.plugins.delphi.type.Type;

public final class FileTypeNode extends TypeNode {
  public FileTypeNode(Token token) {
    super(token);
  }

  @Override
  public <T> T accept(DelphiParserVisitor<T> visitor, T data) {
    return visitor.visit(this, data);
  }

  @Nullable
  private TypeNode getTypeNode() {
    return (TypeNode) jjtGetChild(1);
  }

  @Override
  @NotNull
  public Type createType() {
    TypeNode type = getTypeNode();
    return type == null ? getTypeFactory().untypedFile() : getTypeFactory().fileOf(type.getType());
  }
}
