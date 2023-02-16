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

import org.sonar.plugins.communitydelphi.api.symbol.NameOccurrence;
import org.sonar.plugins.communitydelphi.api.symbol.Invocable;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Type.HelperType;
import org.sonar.plugins.communitydelphi.api.type.Type.StructType;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.GenerifiableDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodDirective;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.VariableNameDeclaration;
import org.sonar.plugins.communitydelphi.api.symbol.scope.DelphiScope;
import org.sonar.plugins.communitydelphi.api.symbol.scope.TypeScope;

public class DelphiScopeImpl implements DelphiScope {
  private final Set<NameDeclaration> declarationSet;
  private final ListMultimap<NameDeclaration, NameOccurrence> occurrencesByDeclaration;
  private final SetMultimap<Class<? extends NameDeclaration>, NameDeclaration> declarationsByClass;
  private final SetMultimap<String, NameDeclaration> declarationsByName;
  private final Map<Type, HelperType> helpersByType;

  private DelphiScope parent;

  protected DelphiScopeImpl() {
    declarationSet = new HashSet<>();
    occurrencesByDeclaration = ArrayListMultimap.create();
    declarationsByClass = HashMultimap.create();
    declarationsByName = TreeMultimap.create(String.CASE_INSENSITIVE_ORDER, Ordering.natural());
    helpersByType = new HashMap<>();
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
    declarationsByClass.put(declaration.getClass(), declaration);
    handleHelperDeclaration(declaration);
  }

  private void checkForwardTypeDeclarations(NameDeclaration typeDeclaration) {
    if (!(typeDeclaration instanceof TypeNameDeclaration)) {
      return;
    }

    TypeNameDeclaration fullTypeDeclaration = (TypeNameDeclaration) typeDeclaration;
    if (fullTypeDeclaration.isGeneric()) {
      // Generic types cannot be forward-declared
      return;
    }

    Type fullType = fullTypeDeclaration.getType();
    if (!fullType.isStruct()) {
      return;
    }

    declarationsByName
        .get(typeDeclaration.getName())
        .removeIf(
            declaration -> {
              if (declaration instanceof TypeNameDeclaration) {
                TypeNameDeclaration forwardDeclaration = (TypeNameDeclaration) declaration;
                if (forwardDeclaration.isGeneric()) {
                  // Generic types cannot be forward-declared
                  return false;
                }

                Type forwardType = forwardDeclaration.getType();
                if (forwardType.isStruct()) {
                  ((StructType) forwardType).setFullType((StructType) fullType);
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

    throw new RuntimeException(declaration.getImage() + " is already in the symbol table");
  }

  private boolean isAcceptableDuplicate(NameDeclaration declaration) {
    if (declaration instanceof Invocable) {
      return true;
    }

    Set<NameDeclaration> duplicates = declarationsByName.get(declaration.getImage());

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
        helpersByType.put(helper.extendedType(), helper);
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

  private void handleGenerics(NameOccurrence occurrence, Set<NameDeclaration> result) {
    int typeArgumentCount = occurrence.getTypeArguments().size();
    result.removeIf(
        declaration -> {
          if (typeArgumentCount == 0 && declaration instanceof MethodNameDeclaration) {
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
   * If the result set is populated with only Method declarations that are marked as overloads, then
   * additional overloads will be searched for and populated into the result set.
   *
   * @param occurrence The name occurrence that we're accumulating declarations for
   * @param result The set of declarations that overloads will be added to, if applicable
   */
  public void findMethodOverloads(NameOccurrence occurrence, Set<NameDeclaration> result) {
    if (result.isEmpty() || !result.stream().allMatch(DelphiScopeImpl::canBeOverloaded)) {
      return;
    }

    for (MethodNameDeclaration declaration : this.getMethodDeclarations()) {
      if (isMethodOverload(declaration, occurrence, result, overloadsRequireOverloadDirective())) {
        result.add(declaration);
      }
    }

    DelphiScope searchScope = overloadSearchScope();
    if (searchScope != null) {
      ((DelphiScopeImpl) searchScope).findMethodOverloads(occurrence, result);
    }
  }

  @Nullable
  protected DelphiScope overloadSearchScope() {
    return parent;
  }

  private static boolean canBeOverloaded(NameDeclaration declaration) {
    if (declaration instanceof MethodNameDeclaration) {
      MethodNameDeclaration methodDeclaration = (MethodNameDeclaration) declaration;
      return !methodDeclaration.isCallable()
          || methodDeclaration.hasDirective(MethodDirective.OVERLOAD)
          || isOverrideForOverloadedMethod(methodDeclaration);
    }
    return false;
  }

  private static boolean isOverrideForOverloadedMethod(MethodNameDeclaration method) {
    if (method.hasDirective(MethodDirective.OVERRIDE)) {
      DelphiScope scope = method.getScope().getEnclosingScope(TypeScope.class).getSuperTypeScope();

      while (scope instanceof TypeScope) {
        MethodNameDeclaration overridden =
            scope.getMethodDeclarations().stream()
                .filter(ancestor -> ancestor.getImage().equalsIgnoreCase(method.getName()))
                .filter(ancestor -> overridesMethodSignature(ancestor, method))
                .findFirst()
                .orElse(null);

        if (overridden != null) {
          return overridden.hasDirective(MethodDirective.VIRTUAL)
              || overridden.hasDirective(MethodDirective.DYNAMIC);
        }

        scope = ((TypeScope) scope).getSuperTypeScope();
      }
    }
    return false;
  }

  private static boolean isMethodOverload(
      MethodNameDeclaration declaration,
      NameOccurrence occurrence,
      Set<NameDeclaration> matchedMethods,
      boolean requireOverloadDirective) {
    return (!requireOverloadDirective || declaration.hasDirective(MethodDirective.OVERLOAD))
        && declaration.getImage().equalsIgnoreCase(occurrence.getImage())
        && matchedMethods.stream()
            .map(MethodNameDeclaration.class::cast)
            .noneMatch(matched -> overridesMethodSignature(matched, declaration));
  }

  protected boolean overloadsRequireOverloadDirective() {
    return false;
  }

  private static boolean overridesMethodSignature(
      MethodNameDeclaration declaration, MethodNameDeclaration overridden) {
    return declaration.isCallable()
        && overridden.isCallable()
        && declaration.hasSameParameterTypes(overridden);
  }

  @Override
  public Set<NameDeclaration> findDeclaration(NameOccurrence occurrence) {
    Set<NameDeclaration> result = Collections.emptySet();

    Set<NameDeclaration> found = declarationsByName.get(occurrence.getImage());
    if (!found.isEmpty()) {
      result = new HashSet<>(found);
      findMethodOverloads(occurrence, result);
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
    return helpersByType.get(type);
  }

  private static NameDeclaration getDeclaration(NameDeclaration declaration) {
    NameDeclaration result = declaration;
    while (result.isSpecializedDeclaration()) {
      result = result.getGenericDeclaration();
    }
    return result;
  }

  @SuppressWarnings("unchecked")
  private <T extends NameDeclaration> Set<T> getDeclarationSet(Class<T> clazz) {
    return (Set<T>) declarationsByClass.get(clazz);
  }

  public Set<UnitNameDeclaration> getUnitDeclarations() {
    return getDeclarationSet(UnitNameDeclaration.class);
  }

  public Set<UnitImportNameDeclaration> getImportDeclarations() {
    return getDeclarationSet(UnitImportNameDeclaration.class);
  }

  public Set<TypeNameDeclaration> getTypeDeclarations() {
    return getDeclarationSet(TypeNameDeclaration.class);
  }

  public Set<PropertyNameDeclaration> getPropertyDeclarations() {
    return getDeclarationSet(PropertyNameDeclaration.class);
  }

  public Set<MethodNameDeclaration> getMethodDeclarations() {
    return getDeclarationSet(MethodNameDeclaration.class);
  }

  public Set<VariableNameDeclaration> getVariableDeclarations() {
    return getDeclarationSet(VariableNameDeclaration.class);
  }
}
