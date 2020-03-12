package org.sonar.plugins.delphi.symbol.declaration;

import com.google.common.collect.ComparisonChain;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyNode;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.resolve.Invocable;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.TypeSpecializationContext;

public final class PropertyNameDeclaration extends AbstractDelphiNameDeclaration
    implements TypedDeclaration, Invocable {

  private final List<ParameterDeclaration> parameters;
  private final boolean isClassInvocable;
  private final boolean isDefaultProperty;
  private final Type type;
  private int hashCode;

  public PropertyNameDeclaration(
      PropertyNode node, @Nullable PropertyNameDeclaration concreteDeclaration) {
    this(
        new SymbolicNode(node.getPropertyName()),
        extractParameters(node, concreteDeclaration),
        node.isClassProperty(),
        node.isDefaultProperty(),
        extractType(node, concreteDeclaration));
  }

  private PropertyNameDeclaration(
      SymbolicNode location,
      List<ParameterDeclaration> parameters,
      boolean isClassInvocable,
      boolean isDefaultProperty,
      Type type) {
    super(location);
    this.parameters = parameters;
    this.isClassInvocable = isClassInvocable;
    this.isDefaultProperty = isDefaultProperty;
    this.type = type;
  }

  private static List<ParameterDeclaration> extractParameters(
      PropertyNode node, @Nullable PropertyNameDeclaration concreteDeclaration) {
    if (concreteDeclaration != null) {
      return concreteDeclaration.getParameters();
    }
    return node.getParameters().stream()
        .map(ParameterDeclaration::create)
        .collect(Collectors.toUnmodifiableList());
  }

  private static Type extractType(
      PropertyNode node, @Nullable PropertyNameDeclaration concreteDeclaration) {
    if (concreteDeclaration != null) {
      return concreteDeclaration.getType();
    }
    return node.getType();
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
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
  protected DelphiNameDeclaration doSpecialization(TypeSpecializationContext context) {
    return new PropertyNameDeclaration(
        getNode(),
        parameters.stream()
            .map(parameter -> parameter.specialize(context))
            .collect(Collectors.toUnmodifiableList()),
        isClassInvocable,
        isDefaultProperty,
        type.specialize(context));
  }

  @Override
  public boolean equals(Object other) {
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
  public int compareTo(@NotNull DelphiNameDeclaration other) {
    int result = super.compareTo(other);
    if (result == 0) {
      PropertyNameDeclaration that = (PropertyNameDeclaration) other;
      result =
          ComparisonChain.start()
              .compare(getParametersCount(), that.getParametersCount())
              .compare(getRequiredParametersCount(), that.getRequiredParametersCount())
              .compareTrueFirst(isClassInvocable, that.isClassInvocable)
              .compare(type.getImage(), that.type.getImage())
              .result();

      if (result != 0) {
        return result;
      }

      if (!equals(other)) {
        result = -1;
      }
    }
    return result;
  }

  @Override
  public String toString() {
    return "Property: image = '"
        + node.getImage()
        + "', line = "
        + node.getBeginLine()
        + ", params = "
        + parameters.size()
        + " <"
        + getNode().getUnitName()
        + ">";
  }
}
