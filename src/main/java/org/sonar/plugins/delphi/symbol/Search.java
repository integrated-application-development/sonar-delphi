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
package org.sonar.plugins.delphi.symbol;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeParameterNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.delphi.symbol.scope.DelphiScope;
import org.sonar.plugins.delphi.symbol.scope.FileScope;
import org.sonar.plugins.delphi.symbol.scope.MethodScope;
import org.sonar.plugins.delphi.symbol.scope.TypeScope;
import org.sonar.plugins.delphi.symbol.scope.WithScope;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.HelperType;
import org.sonar.plugins.delphi.type.Type.ScopedType;

public class Search {
  private static final Logger LOG = Loggers.get(Search.class);
  private static final boolean TRACE = false;

  private final DelphiNameOccurrence occurrence;
  private final Set<NameDeclaration> declarations = new HashSet<>();
  private final SearchMode mode;
  private final Set<NameDeclaration> enclosingTypeResults;

  public Search(DelphiNameOccurrence occurrence, SearchMode mode) {
    if (TRACE) {
      LOG.info("new search for reference " + occurrence + " in mode " + mode);
    }
    this.occurrence = occurrence;
    this.mode = mode;
    this.enclosingTypeResults = new HashSet<>();
  }

  public void execute(DelphiScope startingScope) {
    Set<NameDeclaration> found = searchUpward(startingScope);

    if (!enclosingTypeResults.isEmpty()) {
      FileScope occurrenceFileScope =
          Objects.requireNonNull(
              occurrence.getLocation().getScope().getEnclosingScope(FileScope.class));

      boolean resultsWithinThisFile =
          found.stream()
              .anyMatch(
                  declaration ->
                      occurrenceFileScope.equals(
                          declaration.getScope().getEnclosingScope(FileScope.class)));

      if (!resultsWithinThisFile) {
        // Results from enclosing type scopes take precedence over results from other file scopes.
        found = enclosingTypeResults;
      }
    }

    if (TRACE) {
      LOG.info("search finished, found " + found);
    }

    declarations.addAll(found);
    enclosingTypeResults.clear();
  }

  public Set<NameDeclaration> getResult() {
    return declarations;
  }

  private Set<NameDeclaration> searchUpward(DelphiScope scope) {
    if (TRACE) {
      LOG.info(" checking scope " + scope + " for name occurrence " + occurrence);
    }

    Set<NameDeclaration> result = findDeclaration(scope);

    if (result.isEmpty() && scope.getParent() != null) {
      if (TRACE) {
        LOG.info(" moving up from " + scope + " to " + scope.getParent());
      }
      return searchUpward(scope.getParent());
    }

    return result;
  }

  private Set<NameDeclaration> findDeclaration(DelphiScope scope) {
    if (scope instanceof WithScope) {
      scope = ((WithScope) scope).getTargetScope();
    }

    if (mode != SearchMode.METHOD_HEADING && scope instanceof TypeScope) {
      return searchTypeScope((TypeScope) scope);
    }

    Set<NameDeclaration> result = scope.findDeclaration(occurrence);

    if (scope instanceof TypeScope) {
      result = filterTypeScopeResults(result);
    }

    if (result.isEmpty() && scope instanceof MethodScope) {
      DelphiScope typeScope = ((MethodScope) scope).getTypeScope();
      if (typeScope instanceof TypeScope) {
        result = searchTypeScope((TypeScope) typeScope);
        if (result.isEmpty()) {
          enclosingTypeResults.addAll(searchEnclosingTypes(typeScope.getParent()));
        }
      }
    }
    return result;
  }

  private Set<NameDeclaration> filterTypeScopeResults(Set<NameDeclaration> result) {
    if (mode == SearchMode.METHOD_HEADING) {
      result =
          result.stream()
              .filter(
                  declaration ->
                      declaration instanceof TypeNameDeclaration
                          || declaration instanceof TypeParameterNameDeclaration
                          || declaration instanceof VariableNameDeclaration)
              .collect(Collectors.toSet());
    }
    return result;
  }

  /**
   * Searches a type scope for declarations. This also has to take class/record helpers into
   * account.
   *
   * @see <a href="https://wiki.freepascal.org/Helper_types#Implementation">FreePascal: Helper
   *     types</a>
   * @param scope The type scope to search
   * @return Set of name declarations
   */
  private Set<NameDeclaration> searchTypeScope(TypeScope scope) {
    if (TRACE) {
      LOG.info(" checking type scope " + scope + " for name occurrence " + occurrence);
    }

    Type type = scope.getType();
    Set<NameDeclaration> result = filterTypeScopeResults(searchHelperScope(type));

    if (result.isEmpty()) {
      result = filterTypeScopeResults(scope.findDeclaration(occurrence));
    }

    if (result.isEmpty()) {
      DelphiScope ancestorType = scope.getSuperTypeScope();
      if (ancestorType instanceof TypeScope) {
        if (TRACE) {
          LOG.info(" moving up from type scope " + scope + " to type scope " + ancestorType);
        }

        result = searchTypeScope((TypeScope) ancestorType);
      }
    }

    if (result.isEmpty() && type.isHelper()) {
      result = filterTypeScopeResults(searchExtendedType((HelperType) type));
    }

    return result;
  }

  private Set<NameDeclaration> searchEnclosingTypes(@Nullable DelphiScope scope) {
    Set<NameDeclaration> result = Collections.emptySet();
    if (scope != null) {
      TypeScope nextTypeScope = scope.getEnclosingScope(TypeScope.class);
      if (nextTypeScope != null) {
        if (TRACE) {
          LOG.info("  checking enclosing type scope " + nextTypeScope);
        }
        result = filterTypeScopeResults(nextTypeScope.findDeclaration(occurrence));
        if (result.isEmpty()) {
          return searchEnclosingTypes(nextTypeScope.getParent());
        }
      }
    }
    return result;
  }

  private Set<NameDeclaration> searchExtendedType(HelperType helperType) {
    Type extendedType = helperType.extendedType();
    if (extendedType instanceof ScopedType) {
      DelphiScope extendedTypeScope = ((ScopedType) extendedType).typeScope();
      if (extendedTypeScope instanceof TypeScope) {
        if (TRACE) {
          LOG.info(" moving into extended type scope " + extendedTypeScope);
        }
        return searchTypeScope((TypeScope) extendedTypeScope);
      }
    }
    return Collections.emptySet();
  }

  /**
   * Locates and searches a helper for declarations
   *
   * @see <a href="https://wiki.freepascal.org/Helper_types#Implementation">FreePascal: Helper
   *     types</a>
   * @param type The type to locate a helper for
   * @return Set of name declarations
   */
  private Set<NameDeclaration> searchHelperScope(Type type) {
    Set<NameDeclaration> result = Collections.emptySet();
    DelphiScope locationScope = occurrence.getLocation().getScope();
    Type helperType = locationScope.getHelperForType(type);

    if (helperType != null) {
      while (result.isEmpty() && helperType.isHelper()) {
        DelphiScope helperScope = ((ScopedType) helperType).typeScope();
        if (TRACE) {
          LOG.info(" checking helper scope " + helperScope + " for name occurrence " + occurrence);
        }
        result = helperScope.findDeclaration(occurrence);
        helperType = helperType.superType();
      }
    }

    return result;
  }
}
