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

import au.com.integradev.delphi.symbol.DelphiNameOccurrence;
import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Type.HelperType;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.symboltable.Scope;

public final class UnknownScope implements DelphiScope {
  private static final UnknownScope UNKNOWN_SCOPE = new UnknownScope();

  private UnknownScope() {
    // Hide constructor
  }

  static UnknownScope instance() {
    return UNKNOWN_SCOPE;
  }

  @Override
  public <T extends Scope> T getEnclosingScope(Class<T> clazz) {
    return null;
  }

  @Override
  public Map<NameDeclaration, List<NameOccurrence>> getDeclarations() {
    return Collections.emptyMap();
  }

  @Override
  public <T extends NameDeclaration> Map<T, List<NameOccurrence>> getDeclarations(Class<T> clazz) {
    return Collections.emptyMap();
  }

  @Override
  public boolean contains(NameOccurrence nameOccurrence) {
    return false;
  }

  @Override
  public Set<NameDeclaration> addNameOccurrence(NameOccurrence nameOccurrence) {
    return emptySet();
  }

  @Override
  public Set<NameDeclaration> findDeclaration(DelphiNameOccurrence occurrence) {
    return emptySet();
  }

  @Override
  public void addDeclaration(NameDeclaration nameDeclaration) {
    // Do nothing
  }

  @Override
  public void findMethodOverloads(DelphiNameOccurrence occurrence, Set<NameDeclaration> result) {
    // Do nothing
  }

  @Override
  public DelphiScope getParent() {
    return null;
  }

  @Override
  public void setParent(Scope scope) {
    // Do nothing
  }

  @Override
  public Set<NameDeclaration> getAllDeclarations() {
    return emptySet();
  }

  @Override
  public <T extends NameDeclaration> Set<T> getDeclarationSet(Class<T> clazz) {
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
}
