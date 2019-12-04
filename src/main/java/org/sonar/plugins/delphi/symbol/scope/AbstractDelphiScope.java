package org.sonar.plugins.delphi.symbol.scope;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.AbstractScope;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import org.jetbrains.annotations.NotNull;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.DelphiNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.MethodDirective;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.ParameterDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.resolve.Invocable;
import org.sonar.plugins.delphi.type.DelphiEnumerationType;
import org.sonar.plugins.delphi.type.Type;

public abstract class AbstractDelphiScope extends AbstractScope implements DelphiScope {

  @Override
  public void addDeclaration(NameDeclaration declaration) {
    checkForwardTypeDeclarations(declaration);
    checkForDuplicatedNameDeclaration(declaration);
    super.addDeclaration(declaration);
  }

  private void checkForDuplicatedNameDeclaration(NameDeclaration declaration) {
    if (!(declaration instanceof Invocable) && getDeclarations().keySet().contains(declaration)) {
      throw new RuntimeException(declaration + " is already in the symbol table");
    }
  }

  private void checkForwardTypeDeclarations(NameDeclaration typeDeclaration) {
    if (typeDeclaration instanceof TypeNameDeclaration) {
      for (TypeNameDeclaration declaration : getTypeDeclarations().keySet()) {
        if (declaration.getImage().equalsIgnoreCase(typeDeclaration.getImage())) {
          declaration.setIsForwardDeclaration(((TypeNameDeclaration) typeDeclaration).getType());
          break;
        }
      }
    }
  }

  @Override
  public final Set<NameDeclaration> addNameOccurrence(@NotNull NameOccurrence occurrence) {
    DelphiNameDeclaration declaration = ((DelphiNameOccurrence) occurrence).getNameDeclaration();
    getDeclarations().get(declaration).add(occurrence);
    return Collections.singleton(declaration);
  }

  @Override
  public void findMethodOverloads(DelphiNameOccurrence occurrence, Set<NameDeclaration> result) {
    if (result.isEmpty()
        || !result.stream().allMatch(AbstractDelphiScope::hasOverloadMethodDirective)) {
      return;
    }

    for (MethodNameDeclaration declaration : this.getMethodDeclarations().keySet()) {
      if (isMethodOverload(declaration, occurrence, result)) {
        result.add(declaration);
      }
    }

    DelphiScope parent = this.getParent();
    if (parent != null) {
      parent.findMethodOverloads(occurrence, result);
    }
  }

  private static boolean isMethodOverload(
      MethodNameDeclaration declaration,
      DelphiNameOccurrence occurrence,
      Set<NameDeclaration> matchedMethods) {
    if (!isMatchingDeclaration(declaration, occurrence.getImage())) {
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
    Set<NameDeclaration> result = new HashSet<>();

    searchDeclarations(occurrence, getVariableDeclarations(), result);

    if (result.isEmpty()) {
      searchDeclarations(occurrence, getMethodDeclarations(), result);
      findMethodOverloads(occurrence, result);
    }

    if (result.isEmpty()) {
      searchDeclarations(occurrence, getPropertyDeclarations(), result);
    }

    if (result.isEmpty()) {
      searchTypeDeclarations(occurrence, result);
    }

    return result;
  }

  private void searchDeclarations(
      DelphiNameOccurrence occurrence,
      Map<? extends NameDeclaration, List<NameOccurrence>> declarations,
      Set<NameDeclaration> result) {
    for (NameDeclaration declaration : declarations.keySet()) {
      if (isMatchingDeclaration(declaration, occurrence.getImage())) {
        result.add(declaration);
      }
    }
  }

  private void searchTypeDeclarations(
      DelphiNameOccurrence occurrence, Set<NameDeclaration> result) {
    for (TypeNameDeclaration declaration : getTypeDeclarations().keySet()) {
      if (isMatchingDeclaration(declaration, occurrence.getImage())) {
        result.add(declaration);
      }

      Type type = declaration.getType();
      if (type instanceof DelphiEnumerationType) {
        DelphiScope enumScope = ((DelphiEnumerationType) type).typeScope();
        result.addAll(enumScope.findDeclaration(occurrence));
      }
    }
  }

  private static boolean isMatchingDeclaration(NameDeclaration declaration, String image) {
    return declaration.getImage().equalsIgnoreCase(image) && isNotForwardDeclaration(declaration);
  }

  private static boolean isNotForwardDeclaration(NameDeclaration declaration) {
    return (!(declaration instanceof TypeNameDeclaration)
        || !((TypeNameDeclaration) declaration).isForwardDeclaration());
  }

  private static boolean hasOverloadMethodDirective(NameDeclaration declaration) {
    return declaration instanceof MethodNameDeclaration
        && ((MethodNameDeclaration) declaration).getDirectives().contains(MethodDirective.OVERLOAD);
  }

  @Override
  public boolean contains(NameOccurrence occurrence) {
    return !findDeclaration((DelphiNameOccurrence) occurrence).isEmpty();
  }

  protected <T> String glomNames(Set<T> s) {
    StringBuilder result = new StringBuilder();
    for (T t : s) {
      result.append(t.toString());
      result.append(',');
    }
    return result.length() == 0 ? "" : result.toString().substring(0, result.length() - 1);
  }

  @Override
  public DelphiScope getParent() {
    return (DelphiScope) super.getParent();
  }
}
