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

import au.com.integradev.delphi.symbol.NameDeclaration;
import au.com.integradev.delphi.symbol.NameOccurrence;
import au.com.integradev.delphi.symbol.declaration.MethodNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.PropertyNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.TypeNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.UnitImportNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.UnitNameDeclaration;
import au.com.integradev.delphi.symbol.declaration.VariableNameDeclaration;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Type.HelperType;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;

public interface DelphiScope {
  void addDeclaration(NameDeclaration var1);

  Set<NameDeclaration> addNameOccurrence(NameOccurrence occurrence);

  /**
   * Find declarations in this scope based off of some name occurrence
   *
   * @param occurrence The name for which we are trying to find a matching declaration
   * @return Set of name declarations matching the name occurrence
   */
  Set<NameDeclaration> findDeclaration(NameOccurrence occurrence);

  /**
   * Find a helper type in this scope based off of some type
   *
   * @param type The type for which we are trying to find a helper
   * @return Helper type for the specified type
   */
  @Nullable
  HelperType getHelperForType(Type type);

  /**
   * If the result set is populated with only Method declarations that are marked as overloads, then
   * additional overloads will be searched for and populated into the result set.
   *
   * @param occurrence The name occurrence that we're accumulating declarations for
   * @param result The set of declarations that overloads will be added to, if applicable
   */
  void findMethodOverloads(NameOccurrence occurrence, Set<NameDeclaration> result);

  @Nullable
  DelphiScope getParent();

  void setParent(DelphiScope parent);

  <T extends DelphiScope> T getEnclosingScope(Class<T> type);

  Map<NameDeclaration, List<NameOccurrence>> getDeclarations();

  <T extends NameDeclaration> Map<T, List<NameOccurrence>> getDeclarations(Class<T> var1);

  List<NameOccurrence> getOccurrencesFor(NameDeclaration declaration);

  Set<NameDeclaration> getAllDeclarations();

  <T extends NameDeclaration> Set<T> getDeclarationSet(Class<T> clazz);

  default Set<UnitNameDeclaration> getUnitDeclarations() {
    return getDeclarationSet(UnitNameDeclaration.class);
  }

  default Set<UnitImportNameDeclaration> getImportDeclarations() {
    return getDeclarationSet(UnitImportNameDeclaration.class);
  }

  default Set<TypeNameDeclaration> getTypeDeclarations() {
    return getDeclarationSet(TypeNameDeclaration.class);
  }

  default Set<PropertyNameDeclaration> getPropertyDeclarations() {
    return getDeclarationSet(PropertyNameDeclaration.class);
  }

  default Set<MethodNameDeclaration> getMethodDeclarations() {
    return getDeclarationSet(MethodNameDeclaration.class);
  }

  default Set<VariableNameDeclaration> getVariableDeclarations() {
    return getDeclarationSet(VariableNameDeclaration.class);
  }

  default boolean contains(NameOccurrence occurrence) {
    return !findDeclaration(occurrence).isEmpty();
  }

  static UnknownScope unknownScope() {
    return UnknownScope.instance();
  }
}
