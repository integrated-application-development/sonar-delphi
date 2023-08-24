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
package au.com.integradev.delphi.symbol.declaration;

import au.com.integradev.delphi.symbol.SymbolicNode;
import com.google.common.collect.ComparisonChain;
import java.util.Objects;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.DelphiNode;
import org.sonar.plugins.communitydelphi.api.ast.Node;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

public abstract class NameDeclarationImpl implements NameDeclaration {
  protected final SymbolicNode node;
  private NameDeclaration genericDeclaration;
  private int hashcode;
  private boolean isForwardDeclaration;
  private NameDeclaration forwardDeclaration;
  private boolean isImplementationDeclaration;

  NameDeclarationImpl(DelphiNode node) {
    this(node, node.getScope());
  }

  NameDeclarationImpl(DelphiNode node, DelphiScope scope) {
    this(new SymbolicNode(node, scope));
  }

  NameDeclarationImpl(SymbolicNode node) {
    this.node = node;
    generateHashCode();
  }

  private void generateHashCode() {
    hashcode =
        Objects.hash(getImage().toLowerCase(), isForwardDeclaration, isImplementationDeclaration);
  }

  @Override
  public Node getNode() {
    return node;
  }

  @Override
  public String getImage() {
    return node.getImage();
  }

  @Override
  public DelphiScope getScope() {
    return node.getScope();
  }

  @Override
  public String getName() {
    return this.getImage();
  }

  @Override
  public final NameDeclaration specialize(TypeSpecializationContext context) {
    if (!context.hasSignatureMismatch()) {
      NameDeclaration specialized = doSpecialization(context);
      if (!specialized.equals(this)) {
        specialized.setGenericDeclaration(this);
        return specialized;
      }
    }
    return this;
  }

  /**
   * This is where we actually do all the work to specialize a declaration.
   *
   * @param context information about the type arguments and parameters
   * @return specialized declaration
   */
  protected NameDeclaration doSpecialization(TypeSpecializationContext context) {
    return this;
  }

  @Override
  public boolean isSpecializedDeclaration() {
    return genericDeclaration != null;
  }

  @Override
  @Nullable
  public NameDeclaration getGenericDeclaration() {
    return genericDeclaration;
  }

  @Override
  public void setGenericDeclaration(NameDeclaration genericDeclaration) {
    this.genericDeclaration = genericDeclaration;
  }

  @Override
  @Nullable
  public NameDeclaration getForwardDeclaration() {
    return forwardDeclaration;
  }

  @Override
  public void setForwardDeclaration(NameDeclaration declaration) {
    this.forwardDeclaration = declaration;
  }

  @Override
  public void setIsForwardDeclaration() {
    this.isForwardDeclaration = true;
    generateHashCode();
  }

  @Override
  public boolean isForwardDeclaration() {
    return isForwardDeclaration;
  }

  @Override
  public void setIsImplementationDeclaration() {
    this.isImplementationDeclaration = true;
    generateHashCode();
  }

  @Override
  public boolean isImplementationDeclaration() {
    return isImplementationDeclaration;
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
    NameDeclaration that = (NameDeclaration) o;
    return getImage().equalsIgnoreCase(that.getImage())
        && isForwardDeclaration == that.isForwardDeclaration()
        && isImplementationDeclaration == that.isImplementationDeclaration();
  }

  @Override
  public int hashCode() {
    return hashcode;
  }

  @Override
  public int compareTo(NameDeclaration other) {
    return ComparisonChain.start()
        .compare(getClass().getName(), other.getClass().getName())
        .compare(getName(), other.getName(), String.CASE_INSENSITIVE_ORDER)
        .compareTrueFirst(isForwardDeclaration, other.isForwardDeclaration())
        .result();
  }
}
