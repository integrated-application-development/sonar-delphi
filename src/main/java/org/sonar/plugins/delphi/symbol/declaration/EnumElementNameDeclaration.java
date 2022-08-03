package org.sonar.plugins.delphi.symbol.declaration;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.EnumElementNode;
import org.sonar.plugins.delphi.type.Type;

public class EnumElementNameDeclaration extends AbstractDelphiNameDeclaration
    implements TypedDeclaration {
  private final Type type;

  public EnumElementNameDeclaration(EnumElementNode node) {
    super(node.getNameDeclarationNode());
    this.type = node.getType();
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o) && type.is(((EnumElementNameDeclaration) o).type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), type.getImage().toLowerCase());
  }

  @Override
  public int compareTo(@NotNull DelphiNameDeclaration other) {
    int result = super.compareTo(other);
    if (result == 0) {
      EnumElementNameDeclaration that = (EnumElementNameDeclaration) other;
      result = type.getImage().compareTo(that.type.getImage());
    }
    return result;
  }

  @Override
  public String toString() {
    return "Enum element: image = '"
        + getNode().getImage()
        + "', line = "
        + getNode().getBeginLine()
        + " <"
        + getNode().getUnitName()
        + ">";
  }
}
