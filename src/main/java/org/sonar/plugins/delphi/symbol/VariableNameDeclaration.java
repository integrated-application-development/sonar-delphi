package org.sonar.plugins.delphi.symbol;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.VarNameDeclarationNode;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public class VariableNameDeclaration extends DelphiNameDeclaration implements Typed {
  private final String image;
  private final Type type;

  public VariableNameDeclaration(VarNameDeclarationNode node) {
    super(node);
    this.image = node.getImage();
    this.type = node.getTypedDeclaration().getType();
  }

  private VariableNameDeclaration(String image, Type type, DelphiScope scope) {
    super(null, scope);
    this.image = image;
    this.type = type;
  }

  public static VariableNameDeclaration compilerVariable(
      String image, Type type, DelphiScope scope) {
    return new VariableNameDeclaration(image, type, scope);
  }

  @Override
  public String getImage() {
    return image;
  }

  @Override
  public VarNameDeclarationNode getNode() {
    return (VarNameDeclarationNode) super.getNode();
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }

  @Override
  public final boolean equals(Object other) {
    if (!(other instanceof VariableNameDeclaration)) {
      return false;
    }
    VariableNameDeclaration that = (VariableNameDeclaration) other;
    return that.image.equalsIgnoreCase(image);
  }

  @Override
  public int hashCode() {
    return image.toLowerCase().hashCode();
  }

  @Override
  public String toString() {
    return "Variable: image = '" + image;
  }
}
