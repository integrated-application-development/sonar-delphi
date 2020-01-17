package org.sonar.plugins.delphi.symbol.declaration;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.VarNameDeclarationNode;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.Type;

public final class VariableNameDeclaration extends DelphiNameDeclaration
    implements TypedDeclaration {
  private final Type type;
  private int hashCode;

  public VariableNameDeclaration(VarNameDeclarationNode node) {
    super(node);
    this.type = node.getTypedDeclaration().getType();
  }

  private VariableNameDeclaration(String image, Type type, DelphiScope scope) {
    super(SymbolicNode.imaginary(image, scope));
    this.type = type;
  }

  public static VariableNameDeclaration compilerVariable(
      String image, Type type, DelphiScope scope) {
    return new VariableNameDeclaration(image, type, scope);
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof VariableNameDeclaration)) {
      return false;
    }
    VariableNameDeclaration that = (VariableNameDeclaration) other;
    return getImage().equalsIgnoreCase(that.getImage());
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode = getImage().toLowerCase().hashCode();
    }
    return hashCode;
  }

  @Override
  public String toString() {
    return "Variable: image = '" + getImage();
  }
}
