package org.sonar.plugins.delphi.symbol.scope;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Collections.singleton;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.SetMultimap;
import com.google.common.collect.TreeMultimap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.symboltable.Scope;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodDirective;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.ParameterDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.resolve.Invocable;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.EnumType;
import org.sonar.plugins.delphi.type.Type.StructType;

class AbstractDelphiScope implements DelphiScope {
  private final Set<NameDeclaration> declarationSet;
  private final ListMultimap<NameDeclaration, NameOccurrence> occurrencesByDeclaration;
  private final SetMultimap<Class<?>, DelphiNameDeclaration> declarationsByClass;
  private final TreeMultimap<String, DelphiNameDeclaration> declarationsByName;
  private final Set<TypeNameDeclaration> enumDeclarations;

  private DelphiScope parent;

  protected AbstractDelphiScope() {
    declarationSet = new HashSet<>();
    occurrencesByDeclaration = ArrayListMultimap.create();
    declarationsByClass = HashMultimap.create();
    declarationsByName = TreeMultimap.create(String.CASE_INSENSITIVE_ORDER, Ordering.natural());
    enumDeclarations = new HashSet<>();
  }

  @Override
  public void addDeclaration(NameDeclaration declaration) {
    DelphiNameDeclaration delphiDeclaration = (DelphiNameDeclaration) declaration;
    checkForwardTypeDeclarations(declaration);
    checkForDuplicatedNameDeclaration(declaration);
    declarationSet.add(declaration);
    declarationsByName.put(declaration.getImage(), delphiDeclaration);
    declarationsByClass.put(declaration.getClass(), delphiDeclaration);
    addEnumDeclaration(declaration);
  }

  private void checkForwardTypeDeclarations(NameDeclaration typeDeclaration) {
    if (!(typeDeclaration instanceof TypeNameDeclaration)) {
      return;
    }

    Type type = ((TypeNameDeclaration) typeDeclaration).getType();
    if (!type.isStruct()) {
      return;
    }

    declarationsByName
        .get(typeDeclaration.getName())
        .removeIf(
            declaration -> {
              if (declaration instanceof TypeNameDeclaration) {
                TypeNameDeclaration forwardDeclaration = (TypeNameDeclaration) declaration;
                Type forwardType = forwardDeclaration.getType();
                if (forwardType.isStruct()) {
                  ((StructType) forwardType).setFullType((StructType) type);
                  return true;
                }
              }
              return false;
            });
  }

  private void checkForDuplicatedNameDeclaration(NameDeclaration declaration) {
    if (declaration instanceof Invocable) {
      return;
    }

    if (declarationsByName.containsKey(declaration.getName())) {
      throw new RuntimeException(declaration + " is already in the symbol table");
    }
  }

  private void addEnumDeclaration(NameDeclaration declaration) {
    if (declaration instanceof TypeNameDeclaration) {
      TypeNameDeclaration typeDeclaration = (TypeNameDeclaration) declaration;
      if (typeDeclaration.getType().isEnum()) {
        enumDeclarations.add(typeDeclaration);
      }
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends NameDeclaration> Set<T> getDeclarationSet(Class<T> clazz) {
    return (Set<T>) declarationsByClass.get(clazz);
  }

  @Override
  public Set<NameDeclaration> addNameOccurrence(@NotNull NameOccurrence occurrence) {
    DelphiNameDeclaration declaration = ((DelphiNameOccurrence) occurrence).getNameDeclaration();
    occurrencesByDeclaration.put(declaration, occurrence);
    return singleton(declaration);
  }

  @Override
  public List<NameOccurrence> getOccurrencesFor(NameDeclaration declaration) {
    return occurrencesByDeclaration.get(declaration);
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

    if (parent != null) {
      parent.findMethodOverloads(occurrence, result);
    }
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
    Set<DelphiNameDeclaration> declarations = declarationsByName.get(occurrence.getImage());
    if (!declarations.isEmpty()) {
      return new HashSet<>(declarations);
    }

    return findDeclarationInsideEnumScopes(occurrence);
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
    checkArgument(DelphiNameDeclaration.class.isAssignableFrom(clazz));
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

  protected <T> String glomNames(Set<T> s) {
    StringBuilder result = new StringBuilder();
    for (T t : s) {
      result.append(t.toString());
      result.append(',');
    }
    return result.length() == 0 ? "" : result.toString().substring(0, result.length() - 1);
  }
}
