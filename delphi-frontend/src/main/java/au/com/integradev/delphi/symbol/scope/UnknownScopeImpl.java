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

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.UnknownScope;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.HelperType;

public final class UnknownScopeImpl extends DelphiScopeImpl implements UnknownScope {
  private static final UnknownScopeImpl UNKNOWN_SCOPE = new UnknownScopeImpl();

  private UnknownScopeImpl() {
    // Hide constructor
  }

  public static UnknownScopeImpl instance() {
    return UNKNOWN_SCOPE;
  }

  @Override
  public Set<NameDeclaration> addNameOccurrence(@Nonnull NameOccurrence nameOccurrence) {
    return emptySet();
  }

  @Override
  public void addDeclaration(NameDeclaration nameDeclaration) {
    // Do nothing
  }

  @Override
  public Set<NameDeclaration> findDeclaration(NameOccurrence occurrence) {
    return emptySet();
  }

  @Override
  public void findMethodOverloads(NameOccurrence occurrence, Set<NameDeclaration> result) {
    // Do nothing
  }

  @Override
  public DelphiScope getParent() {
    return null;
  }

  @Override
  public void setParent(DelphiScope scope) {
    // Do nothing
  }

  @Override
  public <T extends DelphiScope> T getEnclosingScope(Class<T> clazz) {
    return null;
  }

  @Override
  public Set<NameDeclaration> getAllDeclarations() {
    return emptySet();
  }

  @Override
  public List<NameOccurrence> getOccurrencesFor(NameDeclaration declaration) {
    return emptyList();
  }

  @Nullable
  @Override
  public HelperType getHelperForType(Type type) {
    return null;
  }

  @Override
  public boolean contains(NameOccurrence nameOccurrence) {
    return false;
  }
}
