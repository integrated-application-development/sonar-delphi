package org.sonar.plugins.delphi.symbol.scope;

import static com.google.common.base.Preconditions.checkArgument;

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
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.symboltable.Scope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.AbstractDelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.GenerifiableDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodDirective;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.ParameterDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.resolve.Invocable;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.EnumType;
import org.sonar.plugins.delphi.type.Type.HelperType;
import org.sonar.plugins.delphi.type.Type.StructType;

class AbstractDelphiScope implements DelphiScope {
  private final Set<NameDeclaration> declarationSet;
  private final ListMultimap<NameDeclaration, NameOccurrence> occurrencesByDeclaration;
  private final SetMultimap<Class<? extends NameDeclaration>, NameDeclaration> declarationsByClass;
  private final SetMultimap<String, DelphiNameDeclaration> declarationsByName;
  private final Set<TypeNameDeclaration> enumDeclarations;
  private final Map<Type, HelperType> helpersByType;

  private DelphiScope parent;

  protected AbstractDelphiScope() {
    declarationSet = new HashSet<>();
    occurrencesByDeclaration = ArrayListMultimap.create();
    declarationsByClass = HashMultimap.create();
    declarationsByName = TreeMultimap.create(String.CASE_INSENSITIVE_ORDER, Ordering.natural());
    enumDeclarations = new HashSet<>();
    helpersByType = new HashMap<>();
  }

  @Override
  public void addDeclaration(NameDeclaration declaration) {
    DelphiNameDeclaration delphiDeclaration = getDeclaration(declaration);
    checkForwardTypeDeclarations(declaration);
    checkForDuplicatedNameDeclaration(declaration);
    declarationSet.add(declaration);
    declarationsByName.put(declaration.getImage(), delphiDeclaration);
    declarationsByClass.put(declaration.getClass(), delphiDeclaration);
    handleEnumDeclaration(declaration);
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

    throw new RuntimeException(declaration + " is already in the symbol table");
  }

  private boolean isAcceptableDuplicate(NameDeclaration declaration) {
    if (declaration instanceof Invocable) {
      return true;
    }

    Set<DelphiNameDeclaration> duplicates = declarationsByName.get(declaration.getImage());

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

  private void handleEnumDeclaration(NameDeclaration declaration) {
    if (declaration instanceof TypeNameDeclaration) {
      TypeNameDeclaration typeDeclaration = (TypeNameDeclaration) declaration;
      if (typeDeclaration.getType().isEnum()) {
        enumDeclarations.add(typeDeclaration);
      }
    }
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
  public Set<NameDeclaration> getAllDeclarations() {
    return declarationSet;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends NameDeclaration> Set<T> getDeclarationSet(Class<T> clazz) {
    return (Set<T>) declarationsByClass.get(clazz);
  }

  @Override
  public Set<NameDeclaration> addNameOccurrence(@NotNull NameOccurrence occurrence) {
    DelphiNameOccurrence delphiOccurrence = (DelphiNameOccurrence) occurrence;
    DelphiNameDeclaration declaration = getDeclaration(delphiOccurrence.getNameDeclaration());
    occurrencesByDeclaration.put(declaration, occurrence);
    return Set.of(declaration);
  }

  @Override
  public List<NameOccurrence> getOccurrencesFor(NameDeclaration declaration) {
    return occurrencesByDeclaration.get(getDeclaration(declaration));
  }

  @Override
  public void findMethodOverloads(DelphiNameOccurrence occurrence, Set<NameDeclaration> result) {
    if (result.isEmpty()
        || !result.stream().allMatch(AbstractDelphiScope::hasOverloadMethodDirective)) {
      return;
    }

    for (MethodNameDeclaration declaration : this.getMethodDeclarations()) {
      if (isMethodOverload(declaration, occurrence, result)) {
        result.add(declaration);
      }
    }

    DelphiScope searchScope = overloadSearchScope();
    if (searchScope != null) {
      searchScope.findMethodOverloads(occurrence, result);
    }
  }

  @Nullable
  protected DelphiScope overloadSearchScope() {
    return parent;
  }

  private static boolean isMethodOverload(
      MethodNameDeclaration declaration,
      DelphiNameOccurrence occurrence,
      Set<NameDeclaration> matchedMethods) {
    if (!declaration.getImage().equalsIgnoreCase(occurrence.getImage())) {
      return false;
    }

    return matchedMethods.stream()
        .map(MethodNameDeclaration.class::cast)
        .noneMatch(matched -> overridesMethodSignature(matched, declaration));
  }

  private static boolean overridesMethodSignature(
      MethodNameDeclaration declaration, MethodNameDeclaration overridden) {
    if (declaration.getRequiredParametersCount() != overridden.getRequiredParametersCount()) {
      return false;
    }

    for (int i = 0; i < declaration.getRequiredParametersCount(); ++i) {
      ParameterDeclaration declarationParam = declaration.getParameter(i);
      ParameterDeclaration matchedParam = overridden.getParameter(i);
      if (!declarationParam.getType().is(matchedParam.getType())) {
        return false;
      }
    }

    return true;
  }

  @Override
  public Set<NameDeclaration> findDeclaration(DelphiNameOccurrence occurrence) {
    Set<NameDeclaration> result = Collections.emptySet();

    Set<DelphiNameDeclaration> found = declarationsByName.get(occurrence.getImage());
    if (!found.isEmpty()) {
      result = new HashSet<>(found);
      findMethodOverloads(occurrence, result);
    }

    if (result.isEmpty()) {
      result = findDeclarationInsideEnumScopes(occurrence);
    }

    return result;
  }

  private Set<NameDeclaration> findDeclarationInsideEnumScopes(DelphiNameOccurrence occurrence) {
    Set<NameDeclaration> result = new HashSet<>();

    for (TypeNameDeclaration typeDeclaration : enumDeclarations) {
      if (!typeDeclaration.isScopedEnum()) {
        DelphiScope enumScope = ((EnumType) typeDeclaration.getType()).typeScope();
        result.addAll(enumScope.findDeclaration(occurrence));
      }
    }

    return result;
  }

  private static boolean hasOverloadMethodDirective(NameDeclaration declaration) {
    return declaration instanceof MethodNameDeclaration
        && ((MethodNameDeclaration) declaration).getDirectives().contains(MethodDirective.OVERLOAD);
  }

  @Nullable
  @Override
  public DelphiScope getParent() {
    return parent;
  }

  @Override
  public void setParent(Scope parent) {
    this.parent = (DelphiScope) parent;
  }

  @Override
  public Map<NameDeclaration, List<NameOccurrence>> getDeclarations() {
    Map<NameDeclaration, List<NameOccurrence>> result = new HashMap<>();
    for (NameDeclaration declaration : declarationSet) {
      result.put(declaration, List.copyOf(occurrencesByDeclaration.get(declaration)));
    }
    return result;
  }

  @Override
  public <T extends NameDeclaration> Map<T, List<NameOccurrence>> getDeclarations(Class<T> clazz) {
    checkArgument(AbstractDelphiNameDeclaration.class.isAssignableFrom(clazz));
    Map<T, List<NameOccurrence>> result = new HashMap<>();
    for (T declaration : getDeclarationSet(clazz)) {
      result.put(declaration, occurrencesByDeclaration.get(declaration));
    }
    return result;
  }

  @Override
  public <T extends Scope> T getEnclosingScope(Class<T> clazz) {
    for (Scope current = this; current != null; current = current.getParent()) {
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

  protected <T> String glomNames(Set<T> s) {
    StringBuilder result = new StringBuilder();
    for (T t : s) {
      result.append(t.toString());
      result.append(',');
    }
    return result.length() == 0 ? "" : result.toString().substring(0, result.length() - 1);
  }

  private static DelphiNameDeclaration getDeclaration(NameDeclaration declaration) {
    DelphiNameDeclaration result = (DelphiNameDeclaration) declaration;
    while (result.isSpecializedDeclaration()) {
      result = result.getGenericDeclaration();
    }
    return result;
  }
}
