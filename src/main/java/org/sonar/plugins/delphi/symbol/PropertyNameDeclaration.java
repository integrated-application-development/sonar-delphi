package org.sonar.plugins.delphi.symbol;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.antlr.ast.node.NameDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyNode;
import org.sonar.plugins.delphi.symbol.resolve.Invocable;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Typed;

public class PropertyNameDeclaration extends DelphiNameDeclaration implements Typed, Invocable {
  private final PropertyNode property;
  private final List<ParameterDeclaration> parameters;
  private final boolean isClassInvocable;
  private final boolean isDefaultProperty;
  private int hashCode;

  public PropertyNameDeclaration(PropertyNode node) {
    super(node.getPropertyName());
    this.property = node;
    this.parameters =
        node.getParameters().stream()
            .map(ParameterDeclaration::new)
            .collect(Collectors.toUnmodifiableList());
    this.isClassInvocable = property.isClassProperty();
    this.isDefaultProperty = node.isDefaultProperty();
  }

  @Override
  public NameDeclarationNode getNode() {
    return (NameDeclarationNode) super.getNode();
  }

  @Override
  @NotNull
  public Type getType() {
    return property.getType();
  }

  @Override
  public Type getReturnType() {
    return getType();
  }

  @Override
  public List<ParameterDeclaration> getParameters() {
    return parameters;
  }

  @Override
  public boolean isCallable() {
    return true;
  }

  @Override
  public boolean isClassInvocable() {
    return isClassInvocable;
  }

  public boolean isArrayProperty() {
    return !parameters.isEmpty();
  }

  public boolean isDefaultProperty() {
    return isDefaultProperty;
  }

  @Override
  public final boolean equals(Object other) {
    if (!(other instanceof PropertyNameDeclaration)) {
      return false;
    }
    PropertyNameDeclaration that = (PropertyNameDeclaration) other;
    return that.node.getImage().equalsIgnoreCase(node.getImage())
        && parameters.equals(that.parameters);
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode = Objects.hash(node.getImage().toLowerCase(), parameters);
    }
    return hashCode;
  }

  @Override
  public String toString() {
    return "Property: image = '" + node.getImage() + "', line = " + node.getBeginLine();
  }
}
