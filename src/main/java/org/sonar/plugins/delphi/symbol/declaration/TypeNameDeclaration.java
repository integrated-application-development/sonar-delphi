package org.sonar.plugins.delphi.symbol.declaration;

import com.google.common.collect.ComparisonChain;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.GenericDefinitionNode.TypeParameter;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.symbol.Qualifiable;
import org.sonar.plugins.delphi.symbol.QualifiedName;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.TypeSpecializationContext;

public final class TypeNameDeclaration extends AbstractDelphiNameDeclaration
    implements GenerifiableDeclaration, TypedDeclaration, Qualifiable {
  private final QualifiedName qualifiedName;
  private final Type type;
  private final List<TypedDeclaration> typeParameters;
  private boolean isScopedEnum;

  public TypeNameDeclaration(TypeDeclarationNode node) {
    this(
        new SymbolicNode(node.getTypeNameNode().getIdentifier(), node.getScope()),
        node.getType(),
        node.getQualifiedName(),
        node.getTypeNameNode().getTypeParameters().stream()
            .map(TypeParameter::getLocation)
            .map(NameDeclarationNode::getNameDeclaration)
            .map(TypedDeclaration.class::cast)
            .collect(Collectors.toUnmodifiableList()));
  }

  public TypeNameDeclaration(SymbolicNode node, Type type, QualifiedName qualifiedName) {
    this(node, type, qualifiedName, Collections.emptyList());
  }

  public TypeNameDeclaration(
      SymbolicNode node,
      Type type,
      QualifiedName qualifiedName,
      List<TypedDeclaration> typeParameters) {
    super(node);
    this.type = type;
    this.qualifiedName = qualifiedName;
    this.typeParameters = typeParameters;
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }

  @Override
  public QualifiedName getQualifiedName() {
    return qualifiedName;
  }

  public void setIsScopedEnum() {
    isScopedEnum = true;
  }

  public boolean isScopedEnum() {
    return isScopedEnum;
  }

  @Override
  public List<TypedDeclaration> getTypeParameters() {
    return typeParameters;
  }

  @Override
  protected DelphiNameDeclaration doSpecialization(TypeSpecializationContext context) {
    return new TypeNameDeclaration(
        getNode(),
        type.specialize(context),
        qualifiedName,
        typeParameters.stream()
            .map(parameter -> parameter.specialize(context))
            .map(TypedDeclaration.class::cast)
            .collect(Collectors.toUnmodifiableList()));
  }

  @Override
  public boolean equals(Object other) {
    if (super.equals(other)) {
      TypeNameDeclaration that = (TypeNameDeclaration) other;
      return getImage().equalsIgnoreCase(that.getImage())
          && type.is(that.type)
          && typeParameters.equals(that.typeParameters);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        getImage().toLowerCase(), type.getImage().toLowerCase(), getTypeParameters());
  }

  @Override
  public int compareTo(@NotNull DelphiNameDeclaration other) {
    int result = super.compareTo(other);

    if (result == 0) {
      TypeNameDeclaration that = (TypeNameDeclaration) other;
      result =
          ComparisonChain.start()
              .compare(type.getImage(), that.type.getImage(), String.CASE_INSENSITIVE_ORDER)
              .compare(typeParameters.size(), that.typeParameters.size())
              .result();
    }

    return result;
  }

  @Override
  public String toString() {
    return "type " + getType().getImage();
  }
}
