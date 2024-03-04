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
package au.com.integradev.delphi.symbol.scope;

import au.com.integradev.delphi.symbol.declaration.TypeNameDeclarationImpl;
import au.com.integradev.delphi.type.factory.StructTypeImpl;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.Invocable;
import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.GenerifiableDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.RoutineNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.TypeScope;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.HelperType;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;

public class DelphiScopeImpl implements DelphiScope {
  private final Set<NameDeclaration> declarationSet;
  private final ListMultimap<NameDeclaration, NameOccurrence> occurrencesByDeclaration;
  private final SetMultimap<String, NameDeclaration> declarationsByName;
  private final Set<UnitNameDeclaration> unitDeclarations;
  private final Set<UnitImportNameDeclaration> importDeclarations;
  private final Set<TypeNameDeclaration> typeDeclarations;
  private final Set<PropertyNameDeclaration> propertyDeclarations;
  private final Set<RoutineNameDeclaration> routineDeclarations;
  private final Set<VariableNameDeclaration> variableDeclarations;
  private final Map<String, HelperType> helpersByType;

  private DelphiScope parent;

  protected DelphiScopeImpl() {
    declarationSet = new HashSet<>();
    occurrencesByDeclaration = ArrayListMultimap.create();
    declarationsByName = TreeMultimap.create(String.CASE_INSENSITIVE_ORDER, Ordering.natural());
    unitDeclarations = new HashSet<>();
    importDeclarations = new HashSet<>();
    typeDeclarations = new HashSet<>();
    propertyDeclarations = new HashSet<>();
    routineDeclarations = new HashSet<>();
    variableDeclarations = new HashSet<>();
    helpersByType = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
  }

  public Set<NameDeclaration> addNameOccurrence(@Nonnull NameOccurrence occurrence) {
    NameDeclaration declaration = getDeclaration(occurrence.getNameDeclaration());
    occurrencesByDeclaration.put(declaration, occurrence);
    return Set.of(declaration);
  }

  public void addDeclaration(NameDeclaration declaration) {
    checkForwardTypeDeclarations(declaration);
    checkForDuplicatedNameDeclaration(declaration);
    declarationSet.add(declaration);
    declarationsByName.put(declaration.getImage(), declaration);
    addDeclarationByClass(declaration);
    handleHelperDeclaration(declaration);
  }

  private void addDeclarationByClass(NameDeclaration declaration) {
    if (declaration instanceof VariableNameDeclaration) {
      variableDeclarations.add((VariableNameDeclaration) declaration);
    } else if (declaration instanceof RoutineNameDeclaration) {
      routineDeclarations.add((RoutineNameDeclaration) declaration);
    } else if (declaration instanceof PropertyNameDeclaration) {
      propertyDeclarations.add((PropertyNameDeclaration) declaration);
    } else if (declaration instanceof UnitImportNameDeclaration) {
      importDeclarations.add((UnitImportNameDeclaration) declaration);
    } else if (declaration instanceof TypeNameDeclaration) {
      typeDeclarations.add((TypeNameDeclaration) declaration);
    } else if (declaration instanceof UnitNameDeclaration) {
      unitDeclarations.add((UnitNameDeclaration) declaration);
    }
  }

  private void checkForwardTypeDeclarations(NameDeclaration typeDeclaration) {
    if (!(typeDeclaration instanceof TypeNameDeclaration)) {
      return;
    }

    var fullTypeDeclaration = (TypeNameDeclarationImpl) typeDeclaration;
    Type fullType = fullTypeDeclaration.getType();
    if (!fullType.isStruct()) {
      return;
    }

    declarationsByName
        .get(typeDeclaration.getName())
        .removeIf(
            declaration -> {
              if (declaration instanceof TypeNameDeclaration) {
                var forwardDeclaration = (TypeNameDeclarationImpl) declaration;

                // A generic type's forward declaration must also be generic, or vice versa
                if (forwardDeclaration.isGeneric() != fullTypeDeclaration.isGeneric()) {
                  return false;
                }

                // A generic type's forward declaration must have matching type parameters
                if (forwardDeclaration.isGeneric()
                    && forwardDeclaration.getTypeParameters().size()
                        != fullTypeDeclaration.getTypeParameters().size()) {
                  return false;
                }

                Type forwardType = forwardDeclaration.getType();
                if (forwardType.isStruct()) {
                  ((StructTypeImpl) forwardType).setFullType((StructType) fullType);
                  fullTypeDeclaration.setForwardDeclaration(forwardDeclaration);
                  forwardDeclaration.setIsForwardDeclaration();
                  return true;
                }
              }
              return false;
            });
  }

  private void checkForDuplicatedNameDeclaration(NameDeclaration declaration) {
    if (!declarationsByName.containsKey(declaration.getName())) {
      return;
    }

    if (isAcceptableDuplicate(declaration)) {
      return;
    }

    throw new DuplicatedDeclarationException(
        declaration.getImage() + " is already in the symbol table");
  }

  private boolean isAcceptableDuplicate(NameDeclaration declaration) {
    if (declaration instanceof Invocable) {
      return true;
    }

    Set<NameDeclaration> duplicates = declarationsByName.get(declaration.getImage());

    // Unit imports can clash with other declarations, except other imports
    if (declaration instanceof UnitImportNameDeclaration) {
      return duplicates.stream().noneMatch(UnitImportNameDeclaration.class::isInstance);
    } else {
      // Disregard unit imports when checking for duplicates
      duplicates.removeIf(UnitImportNameDeclaration.class::isInstance);
    }

    if (declaration instanceof GenerifiableDeclaration) {
      GenerifiableDeclaration generic = (GenerifiableDeclaration) declaration;
      if (generic.isGeneric()) {
        int typeParamSize = generic.getTypeParameters().size();
        return duplicates.stream()
            .filter(GenerifiableDeclaration.class::isInstance)
            .map(GenerifiableDeclaration.class::cast)
            .allMatch(duplicate -> duplicate.getTypeParameters().size() != typeParamSize);
      }
    }

    return duplicates.stream()
        .allMatch(
            duplicate ->
                duplicate instanceof GenerifiableDeclaration
                    && ((GenerifiableDeclaration) duplicate).isGeneric());
  }

  private void handleHelperDeclaration(NameDeclaration declaration) {
    if (declaration instanceof TypeNameDeclaration) {
      TypeNameDeclaration typeDeclaration = (TypeNameDeclaration) declaration;
      Type type = typeDeclaration.getType();
      if (type.isHelper()) {
        HelperType helper = (HelperType) type;
        helpersByType.put(helper.extendedType().getImage(), helper);
      }
    }
  }

  @Override
  public List<NameOccurrence> getOccurrencesFor(NameDeclaration declaration) {
    return occurrencesByDeclaration.get(getDeclaration(declaration));
  }

  @Override
  public Set<NameDeclaration> getAllDeclarations() {
    return declarationSet;
  }

  private static void handleGenerics(NameOccurrence occurrence, Set<NameDeclaration> result) {
    int typeArgumentCount = occurrence.getTypeArguments().size();
    result.removeIf(
        declaration -> {
          if (typeArgumentCount == 0 && declaration instanceof RoutineNameDeclaration) {
            // Could be an implicit specialization
            return false;
          }

          if (declaration instanceof GenerifiableDeclaration) {
            GenerifiableDeclaration generifiable = (GenerifiableDeclaration) declaration;
            return generifiable.getTypeParameters().size() != typeArgumentCount;
          }

          return occurrence.isGeneric();
        });
  }

  /**
   * If the result set is populated with only routine declarations that are marked as overloads,
   * then additional overloads will be searched for and populated into the result set.
   *
   * @param occurrence The name occurrence that we're accumulating declarations for
   * @param result The set of declarations that overloads will be added to, if applicable
   */
  public void findRoutineOverloads(NameOccurrence occurrence, Set<NameDeclaration> result) {
    if (result.isEmpty() || !result.stream().allMatch(DelphiScopeImpl::canBeOverloaded)) {
      return;
    }

    for (RoutineNameDeclaration declaration : this.getRoutineDeclarations()) {
      if (isRoutineOverload(declaration, occurrence, result, overloadsRequireOverloadDirective())) {
        result.add(declaration);
      }
    }

    DelphiScope searchScope = overloadSearchScope();
    if (searchScope != null) {
      ((DelphiScopeImpl) searchScope).findRoutineOverloads(occurrence, result);
    }
  }

  @Nullable
  protected DelphiScope overloadSearchScope() {
    return parent;
  }

  private static boolean canBeOverloaded(NameDeclaration declaration) {
    if (declaration instanceof RoutineNameDeclaration) {
      RoutineNameDeclaration routineDeclaration = (RoutineNameDeclaration) declaration;
      return !routineDeclaration.isCallable()
          || routineDeclaration.hasDirective(RoutineDirective.OVERLOAD)
          || isOverrideForOverloadedMethod(routineDeclaration);
    }
    return false;
  }

  private static boolean isOverrideForOverloadedMethod(RoutineNameDeclaration method) {
    if (method.hasDirective(RoutineDirective.OVERRIDE)) {
      DelphiScope scope = method.getScope().getEnclosingScope(TypeScope.class).getParentTypeScope();

      while (scope instanceof TypeScope) {
        RoutineNameDeclaration overridden =
            scope.getRoutineDeclarations().stream()
                .filter(ancestor -> ancestor.getImage().equalsIgnoreCase(method.getName()))
                .filter(ancestor -> overridesMethodSignature(ancestor, method))
                .findFirst()
                .orElse(null);

        if (overridden != null) {
          return overridden.hasDirective(RoutineDirective.VIRTUAL)
              || overridden.hasDirective(RoutineDirective.DYNAMIC);
        }

        scope = ((TypeScope) scope).getParentTypeScope();
      }
    }
    return false;
  }

  private static boolean isRoutineOverload(
      RoutineNameDeclaration declaration,
      NameOccurrence occurrence,
      Set<NameDeclaration> matchedRoutines,
      boolean requireOverloadDirective) {
    return (!requireOverloadDirective || declaration.hasDirective(RoutineDirective.OVERLOAD))
        && declaration.getImage().equalsIgnoreCase(occurrence.getImage())
        && matchedRoutines.stream()
            .map(RoutineNameDeclaration.class::cast)
            .noneMatch(matched -> overridesMethodSignature(matched, declaration));
  }

  protected boolean overloadsRequireOverloadDirective() {
    return false;
  }

  private static boolean overridesMethodSignature(
      RoutineNameDeclaration declaration, RoutineNameDeclaration overridden) {
    return declaration.isCallable()
        && overridden.isCallable()
        && declaration.hasSameParameterTypes(overridden);
  }

  @Override
  public Set<NameDeclaration> findDeclaration(NameOccurrence occurrence) {
    Set<NameDeclaration> result = Collections.emptySet();

    Set<NameDeclaration> found = declarationsByName.get(occurrence.getImage());
    if (occurrence.isAttributeReference()) {
      found = new HashSet<>(found);
      found.addAll(declarationsByName.get(occurrence.getImage() + "Attribute"));
    }

    if (!found.isEmpty()) {
      result = new HashSet<>(found);
      findRoutineOverloads(occurrence, result);
      handleGenerics(occurrence, result);
    }

    return result;
  }

  @Nullable
  @Override
  public DelphiScope getParent() {
    return parent;
  }

  public void setParent(DelphiScope parent) {
    this.parent = parent;
  }

  @Override
  public <T extends DelphiScope> T getEnclosingScope(Class<T> clazz) {
    for (DelphiScope current = this; current != null; current = current.getParent()) {
      if (clazz.isAssignableFrom(current.getClass())) {
        return clazz.cast(current);
      }
    }
    return null;
  }

  @Nullable
  @Override
  public HelperType getHelperForType(Type type) {
    HelperType result = findHelper(type);
    if (result == null && parent != null) {
      result = parent.getHelperForType(type);
    }
    return result;
  }

  protected HelperType findHelper(Type type) {
    if (helpersByType.isEmpty()) {
      return null;
    }
    return helpersByType.get(type.getImage());
  }

  private static NameDeclaration getDeclaration(NameDeclaration declaration) {
    NameDeclaration result = declaration;
    while (result.isSpecializedDeclaration()) {
      result = result.getGenericDeclaration();
    }
    return result;
  }

  @Override
  public Set<UnitNameDeclaration> getUnitDeclarations() {
    return Collections.unmodifiableSet(unitDeclarations);
  }

  @Override
  public Set<UnitImportNameDeclaration> getImportDeclarations() {
    return Collections.unmodifiableSet(importDeclarations);
  }

  @Override
  public Set<TypeNameDeclaration> getTypeDeclarations() {
    return Collections.unmodifiableSet(typeDeclarations);
  }

  @Override
  public Set<PropertyNameDeclaration> getPropertyDeclarations() {
    return Collections.unmodifiableSet(propertyDeclarations);
  }

  @Override
  public Set<RoutineNameDeclaration> getRoutineDeclarations() {
    return Collections.unmodifiableSet(routineDeclarations);
  }

  @Override
  public Set<VariableNameDeclaration> getVariableDeclarations() {
    return Collections.unmodifiableSet(variableDeclarations);
  }
}
