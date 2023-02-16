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
package org.sonar.plugins.communitydelphi.api.symbol.scope;

import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import au.com.integradev.delphi.symbol.scope.UnknownScopeImpl;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.HelperType;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;

public interface DelphiScope {
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

  @Nullable
  DelphiScope getParent();

  <T extends DelphiScope> T getEnclosingScope(Class<T> type);

  List<NameOccurrence> getOccurrencesFor(NameDeclaration declaration);

  Set<NameDeclaration> getAllDeclarations();

  Set<UnitNameDeclaration> getUnitDeclarations();

  Set<UnitImportNameDeclaration> getImportDeclarations();

  Set<TypeNameDeclaration> getTypeDeclarations();

  Set<PropertyNameDeclaration> getPropertyDeclarations();

  Set<MethodNameDeclaration> getMethodDeclarations();

  Set<VariableNameDeclaration> getVariableDeclarations();

  default boolean contains(NameOccurrence occurrence) {
    return !findDeclaration(occurrence).isEmpty();
  }

  static DelphiScope unknownScope() {
    return UnknownScopeImpl.instance();
  }
}
