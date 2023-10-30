/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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
package au.com.integradev.delphi.symbol.occurrence;

import au.com.integradev.delphi.symbol.SymbolicNode;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.type.Type;

public class NameOccurrenceImpl implements NameOccurrence {
  private final SymbolicNode location;
  private NameDeclaration declaration;
  private String image;
  private boolean isExplicitInvocation;
  private boolean isMethodReference;
  private boolean isGeneric;
  private List<Type> typeParameters = Collections.emptyList();

  public NameOccurrenceImpl(DelphiNode concreteNode, String imageOverride) {
    this(concreteNode);
    this.image = imageOverride;
  }

  public NameOccurrenceImpl(DelphiNode concreteNode) {
    this(new SymbolicNode(concreteNode));
  }

  public NameOccurrenceImpl(SymbolicNode symbolicNode) {
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

  public void setNameDeclaration(NameDeclaration declaration) {
    this.declaration = declaration;
  }

  @Override
  public NameDeclaration getNameDeclaration() {
    return declaration;
  }

  public void setIsExplicitInvocation(boolean isExplicitInvocation) {
    this.isExplicitInvocation = isExplicitInvocation;
  }

  @Override
  public boolean isExplicitInvocation() {
    return isExplicitInvocation;
  }

  public void setIsMethodReference() {
    this.isMethodReference = true;
  }

  @Override
  public boolean isMethodReference() {
    return isMethodReference;
  }

  public void setIsGeneric() {
    this.isGeneric = true;
  }

  @Override
  public boolean isGeneric() {
    return isGeneric;
  }

  public void setTypeArguments(List<Type> typeParameters) {
    this.typeParameters = typeParameters;
  }

  @Override
  public List<Type> getTypeArguments() {
    return typeParameters;
  }

  @Override
  public boolean isAttributeReference() {
    return false;
  }

  @SuppressWarnings("EqualsGetClass")
  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NameOccurrenceImpl that = (NameOccurrenceImpl) o;
    return isExplicitInvocation == that.isExplicitInvocation
        && isMethodReference == that.isMethodReference
        && isGeneric == that.isGeneric
        && location.equals(that.location)
        && Objects.equals(declaration, that.declaration)
        && typeParameters.equals(that.typeParameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(
        location,
        declaration,
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
