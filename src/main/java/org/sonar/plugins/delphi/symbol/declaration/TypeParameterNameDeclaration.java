package org.sonar.plugins.delphi.symbol.declaration;

import java.util.Objects;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.TypeParameterType;
import org.sonar.plugins.delphi.type.TypeSpecializationContext;

public class TypeParameterNameDeclaration extends AbstractDelphiNameDeclaration
    implements TypedDeclaration {
  private final TypeParameterType type;

  public TypeParameterNameDeclaration(DelphiNode location, TypeParameterType type) {
    this(new SymbolicNode(location), type);
  }

  private TypeParameterNameDeclaration(SymbolicNode location, TypeParameterType type) {
    super(location);
    this.type = type;
  }

  @NotNull
  @Override
  public TypeParameterType getType() {
    return type;
  }

  @Override
  public DelphiNameDeclaration doSpecialization(TypeSpecializationContext context) {
    Type specialized = type.specialize(context);
    if (specialized.isTypeParameter() && specialized != type) {
      return new TypeParameterNameDeclaration(getNode(), (TypeParameterType) specialized);
    }
    return this;
  }

  @Override
  public boolean equals(Object o) {
    return super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), type);
  }

  @Override
  public String toString() {
    return "type parameter <" + getImage() + ">";
  }
}
