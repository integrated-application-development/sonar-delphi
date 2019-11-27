package org.sonar.plugins.delphi.symbol;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public final class TypeNameDeclaration extends DelphiNameDeclaration implements Typed {
  private boolean isForwardDeclaration;
  private Type type;

  public TypeNameDeclaration(TypeDeclarationNode node) {
    super(node.getTypeNameNode(), node.getScope());
    type = node.getType();
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }

  @Override
  public String toString() {
    return "type " + getType().getImage();
  }

  void setIsForwardDeclaration(Type fullType) {
    isForwardDeclaration = true;
    this.type = fullType;
  }

  public boolean isForwardDeclaration() {
    return isForwardDeclaration;
  }

  @Override
  public boolean equals(Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }
    TypeNameDeclaration that = (TypeNameDeclaration) other;
    return getImage().equalsIgnoreCase(that.getImage())
        && isForwardDeclaration == that.isForwardDeclaration;
  }

  @Override
  public int hashCode() {
    return Objects.hash(getImage().toLowerCase(), isForwardDeclaration);
  }
}
