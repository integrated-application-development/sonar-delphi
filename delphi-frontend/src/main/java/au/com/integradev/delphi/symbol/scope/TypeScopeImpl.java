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
package au.com.integradev.delphi.symbol.scope;

import static org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope.unknownScope;
import static org.sonar.plugins.communitydelphi.api.type.TypeFactory.unknownType;

import au.com.integradev.delphi.type.generic.GenerifiableTypeImpl;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.TypeScope;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.HelperType;
import org.sonar.plugins.communitydelphi.api.type.Type.ScopedType;
import org.sonar.plugins.communitydelphi.api.type.TypeSpecializationContext;

public class TypeScopeImpl extends DelphiScopeImpl implements TypeScope {
  private Type type = unknownType();
  private DelphiScope superTypeScope = unknownScope();

  public void setType(Type type) {
    this.type = type;

    Type superType = type.superType();
    if (superType instanceof ScopedType) {
      this.superTypeScope = ((ScopedType) superType).typeScope();
    }
  }

  @Override
  public DelphiScope getSuperTypeScope() {
    return superTypeScope;
  }

  @Override
  public Type getType() {
    return type;
  }

  @Nullable
  @Override
  protected DelphiScope overloadSearchScope() {
    // A helper type doesn't search for overloads in its ancestors.
    // Instead, it searches for overloads in the extended type.
    // See: https://wiki.freepascal.org/Helper_types#Method_hiding
    if (type.isHelper()) {
      HelperType helperType = ((HelperType) type);
      Type extendedType = helperType.extendedType();
      if (extendedType instanceof ScopedType) {
        return ((ScopedType) extendedType).typeScope();
      }
      return null;
    }
    return superTypeScope;
  }

  @Override
  public String toString() {
    return type.getImage() + " <TypeScope>";
  }

  public static TypeScope specializedScope(
      DelphiScope scope, GenerifiableTypeImpl specializedType, TypeSpecializationContext context) {
    SpecializedTypeScope result = new SpecializedTypeScope(scope, context);
    result.setType(specializedType);
    return result;
  }

  /**
   * Specialized type scopes just wrap a generic type's "real" scope. Name occurrences of
   * specialized declarations are forwarded to their generic declarations in the real scope.
   */
  private static final class SpecializedTypeScope extends TypeScopeImpl {
    private final DelphiScope genericScope;

    private SpecializedTypeScope(DelphiScope scope, TypeSpecializationContext context) {
      this.genericScope = scope;
      scope.getAllDeclarations().stream()
          .map(NameDeclaration.class::cast)
          .map(declaration -> declaration.specialize(context))
          .forEach(super::addDeclaration);
    }

    @Override
    public void addDeclaration(NameDeclaration declaration) {
      throw new UnsupportedOperationException("Can't add declarations to a specialized type scope");
    }

    @Override
    public Set<NameDeclaration> addNameOccurrence(NameOccurrence occurrence) {
      return ((DelphiScopeImpl) genericScope).addNameOccurrence(occurrence);
    }
  }
}
