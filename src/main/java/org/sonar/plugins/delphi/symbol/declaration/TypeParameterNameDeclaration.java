package org.sonar.plugins.delphi.symbol.declaration;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.TypeParameterType;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;

public class TypeParameterNameDeclaration extends AbstractDelphiNameDeclaration
    implements TypedDeclaration {
  private final Type type;

  public TypeParameterNameDeclaration(DelphiNode location, TypeParameterType type) {
    this(new SymbolicNode(location), type);
  }

  private TypeParameterNameDeclaration(SymbolicNode location, Type type) {
    super(location);
    this.type = type;
  }

  @NotNull
  @Override
  public Type getType() {
    return type;
  }

  @Override
  public DelphiNameDeclaration doSpecialization(TypeSpecializationContext context) {
    Type specialized = type.specialize(context);
    if (type.isTypeParameter() && specialized != type) {
      return new TypeParameterNameDeclaration(getNode(), specialized);
    }
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (super.equals(o)) {
      TypeParameterNameDeclaration that = (TypeParameterNameDeclaration) o;
      return type == that.type;
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), type.getImage().toLowerCase());
  }

  @Override
  public String toString() {
    return "type parameter <" + type.getImage() + ">";
  }
}
