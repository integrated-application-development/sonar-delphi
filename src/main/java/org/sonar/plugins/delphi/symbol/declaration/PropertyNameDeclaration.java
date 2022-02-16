package org.sonar.plugins.delphi.symbol.declaration;

import com.google.common.collect.ComparisonChain;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.sourceforge.pmd.lang.ast.Node;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.antlr.ast.node.NameReferenceNode;
import org.sonar.plugins.delphi.antlr.ast.node.PrimaryExpressionNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyReadSpecifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.PropertyWriteSpecifierNode;
import org.sonar.plugins.delphi.antlr.ast.node.TypeDeclarationNode;
import org.sonar.plugins.delphi.antlr.ast.node.Visibility;
import org.sonar.plugins.delphi.symbol.SymbolicNode;
import org.sonar.plugins.delphi.symbol.resolve.Invocable;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.generic.TypeSpecializationContext;
import org.sonar.plugins.delphi.type.parameter.FormalParameter;
import org.sonar.plugins.delphi.type.parameter.Parameter;

public final class PropertyNameDeclaration extends AbstractDelphiNameDeclaration
    implements TypedDeclaration, Invocable, Visibility {

  private final String fullyQualifiedName;
  private final List<Parameter> parameters;
  private final boolean isClassInvocable;
  private final boolean isDefaultProperty;
  private final Type type;
  private final DelphiNameDeclaration readDeclaration;
  private final DelphiNameDeclaration writeDeclaration;
  private final VisibilityType visibility;
  private final List<PropertyNameDeclaration> redeclarations;
  private int hashCode;

  public PropertyNameDeclaration(
      PropertyNode node, @Nullable PropertyNameDeclaration concreteDeclaration) {
    this(
        new SymbolicNode(node.getPropertyName()),
        makeQualifiedName(node),
        extractParameters(node, concreteDeclaration),
        node.isClassProperty(),
        node.isDefaultProperty(),
        extractType(node, concreteDeclaration),
        extractReadDeclaration(node, concreteDeclaration),
        extractWriteDeclaration(node, concreteDeclaration),
        node.getVisibility());
  }

  private PropertyNameDeclaration(
      SymbolicNode location,
      String fullyQualifiedName,
      List<Parameter> parameters,
      boolean isClassInvocable,
      boolean isDefaultProperty,
      Type type,
      DelphiNameDeclaration readDeclaration,
      DelphiNameDeclaration writeDeclaration,
      VisibilityType visibility) {
    super(location);
    this.fullyQualifiedName = fullyQualifiedName;
    this.parameters = parameters;
    this.isClassInvocable = isClassInvocable;
    this.isDefaultProperty = isDefaultProperty;
    this.type = type;
    this.readDeclaration = readDeclaration;
    this.writeDeclaration = writeDeclaration;
    this.visibility = visibility;
    this.redeclarations = new ArrayList<>();
  }

  private static String makeQualifiedName(PropertyNode propertyNode) {
    return propertyNode.getFirstParentOfType(TypeDeclarationNode.class).fullyQualifiedName()
        + "."
        + propertyNode.getPropertyName().getImage();
  }

  private static List<Parameter> extractParameters(
      PropertyNode node, @Nullable PropertyNameDeclaration concreteDeclaration) {
    if (concreteDeclaration != null) {
      return concreteDeclaration.getParameters();
    }
    return node.getParameters().stream()
        .map(FormalParameter::create)
        .collect(Collectors.toUnmodifiableList());
  }

  private static Type extractType(
      PropertyNode node, @Nullable PropertyNameDeclaration concreteDeclaration) {
    if (concreteDeclaration != null) {
      return concreteDeclaration.getType();
    }
    return node.getType();
  }

  private static DelphiNameDeclaration extractReadDeclaration(
      PropertyNode node, @Nullable PropertyNameDeclaration concreteDeclaration) {
    if (concreteDeclaration != null) {
      return concreteDeclaration.getReadDeclaration();
    }

    PropertyReadSpecifierNode readSpecifier = node.getReadSpecifier();
    if (readSpecifier != null) {
      return extractSpecifierDeclaration(readSpecifier.getExpression());
    }

    return null;
  }

  private static DelphiNameDeclaration extractWriteDeclaration(
      PropertyNode node, @Nullable PropertyNameDeclaration concreteDeclaration) {
    if (concreteDeclaration != null) {
      return concreteDeclaration.getWriteDeclaration();
    }

    PropertyWriteSpecifierNode writeSpecifier = node.getWriteSpecifier();
    if (writeSpecifier != null) {
      return extractSpecifierDeclaration(writeSpecifier.getExpression());
    }

    return null;
  }

  private static DelphiNameDeclaration extractSpecifierDeclaration(PrimaryExpressionNode node) {
    DelphiNameDeclaration result = null;
    for (int i = 0; i < node.jjtGetNumChildren(); ++i) {
      Node child = node.jjtGetChild(i);
      if (child instanceof NameReferenceNode) {
        result = ((NameReferenceNode) child).getLastName().getNameDeclaration();
      }
    }
    return result;
  }

  @Override
  @NotNull
  public Type getType() {
    return type;
  }

  public String fullyQualifiedName() {
    return fullyQualifiedName;
  }

  @Nullable
  public DelphiNameDeclaration getReadDeclaration() {
    return readDeclaration;
  }

  @Nullable
  public DelphiNameDeclaration getWriteDeclaration() {
    return writeDeclaration;
  }

  @Override
  public Type getReturnType() {
    return getType();
  }

  @Override
  public List<Parameter> getParameters() {
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

  @Override
  public VisibilityType getVisibility() {
    return visibility;
  }

  public boolean isArrayProperty() {
    return !parameters.isEmpty();
  }

  public boolean isDefaultProperty() {
    return isDefaultProperty;
  }

  public void addRedeclaration(PropertyNameDeclaration declaration) {
    this.redeclarations.add(declaration);
  }

  public List<PropertyNameDeclaration> getRedeclarations() {
    return redeclarations;
  }

  @Override
  protected DelphiNameDeclaration doSpecialization(TypeSpecializationContext context) {
    return new PropertyNameDeclaration(
        getNode(),
        fullyQualifiedName,
        parameters.stream()
            .map(parameter -> parameter.specialize(context))
            .collect(Collectors.toUnmodifiableList()),
        isClassInvocable,
        isDefaultProperty,
        type.specialize(context),
        specializeIfNotNull(readDeclaration, context),
        specializeIfNotNull(writeDeclaration, context),
        visibility);
  }

  private static DelphiNameDeclaration specializeIfNotNull(
      DelphiNameDeclaration declaration, TypeSpecializationContext context) {
    if (declaration != null) {
      return declaration.specialize(context);
    }
    return null;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof PropertyNameDeclaration)) {
      return false;
    }
    PropertyNameDeclaration that = (PropertyNameDeclaration) other;
    return that.node.getImage().equalsIgnoreCase(node.getImage())
        && parameters.equals(that.parameters)
        && type.is(that.type);
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
