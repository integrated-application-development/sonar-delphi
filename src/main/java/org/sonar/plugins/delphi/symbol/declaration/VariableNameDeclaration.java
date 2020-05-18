package org.sonar.plugins.delphi.symbol.declaration;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.Visibility;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

public final class VariableNameDeclaration extends AbstractDelphiNameDeclaration
    implements TypedDeclaration, Visibility {
  private final Type type;
  private final VisibilityType visibility;
  private int hashCode;

  public VariableNameDeclaration(NameDeclarationNode node) {
    this(new SymbolicNode(node), extractType(node), extractVisibility(node));
  }

  private VariableNameDeclaration(String image, Type type, DelphiScope scope) {
    super(SymbolicNode.imaginary(image, scope));
    this.type = type;
    this.visibility = VisibilityType.PUBLIC;
  }

  private VariableNameDeclaration(SymbolicNode location, Type type, VisibilityType visibility) {
    super(location);
    this.type = type;
    this.visibility = visibility;
  }

  public static VariableNameDeclaration compilerVariable(
      String image, Type type, DelphiScope scope) {
    return new VariableNameDeclaration(image, type, scope);
  }

  private static Type extractType(NameDeclarationNode node) {
    Typed typed;

    switch (node.getKind()) {
      case CONST:
      case EXCEPT_ITEM:
        typed = (Typed) node.jjtGetParent();
        break;
      case PARAMETER:
      case FIELD:
      case VAR:
        typed = (Typed) node.getNthParent(2);
        break;
      default:
        throw new AssertionError("Unhandled DeclarationKind");
    }

    return typed.getType();
  }

  private static VisibilityType extractVisibility(NameDeclarationNode node) {
    Visibility visibility;

    switch (node.getKind()) {
      case CONST:
        visibility = (Visibility) node.jjtGetParent();
        break;
      case FIELD:
        visibility = (Visibility) node.getNthParent(2);
        break;
      default:
        return VisibilityType.PUBLIC;
    }

    return visibility.getVisibility();
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }

  @Override
  public VisibilityType getVisibility() {
    return visibility;
  }

  @Override
  protected DelphiNameDeclaration doSpecialization(TypeSpecializationContext context) {
    return new VariableNameDeclaration(getNode(), type.specialize(context), visibility);
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
