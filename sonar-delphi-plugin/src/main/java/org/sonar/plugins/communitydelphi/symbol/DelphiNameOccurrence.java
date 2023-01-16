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
package org.sonar.plugins.communitydelphi.symbol;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.sonar.plugins.communitydelphi.antlr.ast.node.DelphiNode;
import org.sonar.plugins.communitydelphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.communitydelphi.type.Type;

public final class DelphiNameOccurrence implements NameOccurrence {
  private final SymbolicNode location;
  private DelphiNameDeclaration declaration;
  private DelphiNameOccurrence qualifiedName;
  private String image;
  private boolean isExplicitInvocation;
  private boolean isMethodReference;
  private boolean isGeneric;
  private List<Type> typeParameters = Collections.emptyList();

  public DelphiNameOccurrence(DelphiNode concreteNode, String imageOverride) {
    this(concreteNode);
    this.image = imageOverride;
  }

  public DelphiNameOccurrence(DelphiNode concreteNode) {
    this(new SymbolicNode(concreteNode));
  }

  public DelphiNameOccurrence(SymbolicNode symbolicNode) {
    this.location = symbolicNode;
  }

  @Override
  public SymbolicNode getLocation() {
    return location;
  }

  @Override
  public String getImage() {
    if (image == null) {
      return location.getImage();
    }
    return image;
  }

  public void setNameWhichThisQualifies(DelphiNameOccurrence qualifiedName) {
    this.qualifiedName = qualifiedName;
  }

  public DelphiNameOccurrence getNameForWhichThisIsAQualifier() {
    return qualifiedName;
  }

  public void setNameDeclaration(DelphiNameDeclaration declaration) {
    this.declaration = declaration;
  }

  public DelphiNameDeclaration getNameDeclaration() {
    return declaration;
  }

  public boolean isPartOfQualifiedName() {
    return qualifiedName != null;
  }

  public void setIsExplicitInvocation(boolean isExplicitInvocation) {
    this.isExplicitInvocation = isExplicitInvocation;
  }

  public boolean isExplicitInvocation() {
    return isExplicitInvocation;
  }

  public void setIsMethodReference() {
    this.isMethodReference = true;
  }

  public boolean isMethodReference() {
    return isMethodReference;
  }

  public void setIsGeneric() {
    this.isGeneric = true;
  }

  public boolean isGeneric() {
    return isGeneric;
  }

  public void setTypeArguments(List<Type> typeParameters) {
    this.typeParameters = typeParameters;
  }

  public List<Type> getTypeArguments() {
    return typeParameters;
  }

  public boolean isSelf() {
    return "Self".equals(getImage());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DelphiNameOccurrence that = (DelphiNameOccurrence) o;
    return isExplicitInvocation == that.isExplicitInvocation
        && isMethodReference == that.isMethodReference
        && isGeneric == that.isGeneric
        && location.equals(that.location)
        && Objects.equals(declaration, that.declaration)
        && Objects.equals(qualifiedName, that.qualifiedName)
        && typeParameters.equals(that.typeParameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        location,
        declaration,
        qualifiedName,
        image,
        isExplicitInvocation,
        isMethodReference,
        isGeneric,
        typeParameters);
  }

  @Override
  public String toString() {
    return getImage()
        + " ["
        + location.getBeginLine()
        + ","
        + location.getBeginColumn()
        + "] "
        + "<"
        + location.getUnitName()
        + ">";
  }
}
