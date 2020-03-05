package org.sonar.plugins.delphi.symbol.declaration;

import static org.sonar.plugins.delphi.type.DelphiType.unknownType;

import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.SimpleNameDeclarationNode;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.TypeSpecializationContext;
import org.sonar.plugins.delphi.type.Typed;

public final class VariableNameDeclaration extends AbstractDelphiNameDeclaration
    implements TypedDeclaration {
  private final Type type;
  private int hashCode;

  public VariableNameDeclaration(SimpleNameDeclarationNode node) {
    this(new SymbolicNode(node), extractType(node));
  }

  private VariableNameDeclaration(String image, Type type, DelphiScope scope) {
    super(SymbolicNode.imaginary(image, scope));
    this.type = type;
  }

  private VariableNameDeclaration(SymbolicNode location, Type type) {
    super(location);
    this.type = type;
  }

  public static VariableNameDeclaration compilerVariable(
      String image, Type type, DelphiScope scope) {
    return new VariableNameDeclaration(image, type, scope);
  }

  private static Type extractType(SimpleNameDeclarationNode node) {
    Typed typed = null;

    if (node.isConstDeclaration()) {
      typed = ((Typed) node.jjtGetParent());
    } else if (node.isFormalParameter() || node.isFieldDeclaration() || node.isVarDeclaration()) {
      typed = ((Typed) node.getNthParent(2));
    }

    return (typed == null) ? unknownType() : typed.getType();
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }

  @Override
  protected DelphiNameDeclaration doSpecialization(TypeSpecializationContext context) {
    return new VariableNameDeclaration(getNode(), type.specialize(context));
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
