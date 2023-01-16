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
import java.util.HashSet;
import java.util.Set;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;
import org.sonar.plugins.communitydelphi.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.symbol.scope.MethodScope;
import org.sonar.plugins.communitydelphi.symbol.scope.TypeScope;
import org.sonar.plugins.communitydelphi.symbol.scope.WithScope;
import org.sonar.plugins.communitydelphi.type.Type;
import org.sonar.plugins.communitydelphi.type.Type.HelperType;
import org.sonar.plugins.communitydelphi.type.Type.ScopedType;

public class Search {
  private static final Logger LOG = Loggers.get(Search.class);
  private static final boolean TRACE = false;

  private final DelphiNameOccurrence occurrence;
  private final Set<NameDeclaration> declarations = new HashSet<>();

  public Search(DelphiNameOccurrence occurrence) {
    if (TRACE) {
      LOG.info("new search for reference " + occurrence);
    }
    this.occurrence = occurrence;
  }

  public void execute(DelphiScope startingScope) {
    Set<NameDeclaration> found = searchUpward(startingScope);
    if (TRACE) {
      LOG.info("found " + found);
    }
    declarations.addAll(found);
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
    } else if (TRACE) {
      LOG.info(" found it!");
    }

    return result;
  }

  private Set<NameDeclaration> findDeclaration(DelphiScope scope) {
    if (scope instanceof WithScope) {
      scope = ((WithScope) scope).getTargetScope();
    }

    if (scope instanceof TypeScope) {
      return searchTypeScope((TypeScope) scope);
    }

    Set<NameDeclaration> result = scope.findDeclaration(occurrence);

    if (result.isEmpty() && scope instanceof MethodScope) {
      DelphiScope typeScope = ((MethodScope) scope).getTypeScope();
      if (typeScope instanceof TypeScope) {
        result = searchTypeScope((TypeScope) typeScope);
        if (result.isEmpty()) {
          result = searchTopLevelTypeScopes(typeScope.getParent());
        }
      }
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
    Set<NameDeclaration> result = searchHelperScope(type);

    if (result.isEmpty()) {
      result = scope.findDeclaration(occurrence);
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
      result = searchExtendedType((HelperType) type);
    }

    return result;
  }

  private Set<NameDeclaration> searchTopLevelTypeScopes(@Nullable DelphiScope scope) {
    Set<NameDeclaration> result = Collections.emptySet();
    if (scope != null) {
      TypeScope nextTypeScope = scope.getEnclosingScope(TypeScope.class);
      if (nextTypeScope != null) {
        result = nextTypeScope.findDeclaration(occurrence);
        if (result.isEmpty()) {
          result = searchTopLevelTypeScopes(nextTypeScope.getParent());
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
