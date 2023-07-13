/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019-2022 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package au.com.integradev.delphi.symbol.declaration;

import au.com.integradev.delphi.symbol.SymbolicNode;
import au.com.integradev.delphi.type.parameter.FormalParameter;
import com.google.common.collect.ComparisonChain;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.NameReferenceNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.ast.PrimaryExpressionNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyReadSpecifierNode;
import org.sonar.plugins.communitydelphi.api.ast.PropertyWriteSpecifierNode;
import org.sonar.plugins.communitydelphi.api.ast.TypeDeclarationNode;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Parameter;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

public final class PropertyNameDeclarationImpl extends NameDeclarationImpl
    implements PropertyNameDeclaration {

  private final String fullyQualifiedName;
  private final List<Parameter> parameters;
  private final boolean isClassInvocable;
  private final boolean isDefaultProperty;
  private final Type type;
  private final NameDeclaration readDeclaration;
  private final NameDeclaration writeDeclaration;
  private final VisibilityType visibility;
  private final List<PropertyNameDeclaration> redeclarations;
  private int hashCode;

  public PropertyNameDeclarationImpl(
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

  private PropertyNameDeclarationImpl(
      SymbolicNode location,
      String fullyQualifiedName,
      List<Parameter> parameters,
      boolean isClassInvocable,
      boolean isDefaultProperty,
      Type type,
      NameDeclaration readDeclaration,
      NameDeclaration writeDeclaration,
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

  private static NameDeclaration extractReadDeclaration(
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

  private static NameDeclaration extractWriteDeclaration(
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

  private static NameDeclaration extractSpecifierDeclaration(PrimaryExpressionNode node) {
    NameDeclaration result = null;
    for (int i = 0; i < node.getChildrenCount(); ++i) {
      Node child = node.getChild(i);
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

  @Override
  public String fullyQualifiedName() {
    return fullyQualifiedName;
  }

  @Override
  @Nullable
  public NameDeclaration getReadDeclaration() {
    return readDeclaration;
  }

  @Override
  @Nullable
  public NameDeclaration getWriteDeclaration() {
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

  @Override
  public boolean isArrayProperty() {
    return !parameters.isEmpty();
  }

  @Override
  public boolean isDefaultProperty() {
    return isDefaultProperty;
  }

  public void addRedeclaration(PropertyNameDeclaration declaration) {
    this.redeclarations.add(declaration);
  }

  @Override
  public List<PropertyNameDeclaration> getRedeclarations() {
    return redeclarations;
  }

  @Override
  protected NameDeclaration doSpecialization(TypeSpecializationContext context) {
    return new PropertyNameDeclarationImpl(
        node,
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

  private static NameDeclaration specializeIfNotNull(
      NameDeclaration declaration, TypeSpecializationContext context) {
    if (declaration != null) {
      return declaration.specialize(context);
    }
    return null;
  }

  @Override
  public boolean equals(Object other) {
    if (!(other instanceof PropertyNameDeclarationImpl)) {
      return false;
    }
    PropertyNameDeclarationImpl that = (PropertyNameDeclarationImpl) other;
    return that.getNode().getImage().equalsIgnoreCase(getNode().getImage())
        && parameters.equals(that.parameters)
        && type.is(that.type);
  }

  @Override
  public int hashCode() {
    if (hashCode == 0) {
      hashCode = Objects.hash(getNode().getImage().toLowerCase(), parameters);
    }
    return hashCode;
  }

  @Override
  public int compareTo(@NotNull NameDeclaration other) {
    int result = super.compareTo(other);
    if (result == 0) {
      PropertyNameDeclarationImpl that = (PropertyNameDeclarationImpl) other;
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
        + getNode().getImage()
        + "', line = "
        + getNode().getBeginLine()
        + ", params = "
        + parameters.size()
        + " <"
        + getNode().getUnitName()
        + ">";
  }
}
