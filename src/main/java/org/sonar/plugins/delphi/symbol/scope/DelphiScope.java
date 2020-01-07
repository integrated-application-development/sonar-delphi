package org.sonar.plugins.delphi.symbol.scope;

import java.util.List;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.symboltable.Scope;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.symbol.declaration.MethodNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.PropertyNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.TypeNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitImportNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.UnitNameDeclaration;
import org.sonar.plugins.delphi.symbol.declaration.VariableNameDeclaration;

public interface DelphiScope extends Scope {
  Set<NameDeclaration> findDeclaration(DelphiNameOccurrence occurrence);

  void findMethodOverloads(DelphiNameOccurrence occurrence, Set<NameDeclaration> result);

  @Override
  DelphiScope getParent();

  List<NameOccurrence> getOccurrencesFor(NameDeclaration declaration);

  <T extends NameDeclaration> Set<T> getDeclarationSet(Class<T> clazz);

  default Set<UnitNameDeclaration> getUnitDeclarations() {
    return getDeclarationSet(UnitNameDeclaration.class);
  }

  default Set<UnitImportNameDeclaration> getImportDeclarations() {
    return getDeclarationSet(UnitImportNameDeclaration.class);
  }

  default Set<TypeNameDeclaration> getTypeDeclarations() {
    return getDeclarationSet(TypeNameDeclaration.class);
  }

  default Set<PropertyNameDeclaration> getPropertyDeclarations() {
    return getDeclarationSet(PropertyNameDeclaration.class);
  }

  default Set<MethodNameDeclaration> getMethodDeclarations() {
    return getDeclarationSet(MethodNameDeclaration.class);
  }

  default Set<VariableNameDeclaration> getVariableDeclarations() {
    return getDeclarationSet(VariableNameDeclaration.class);
  }

  @Override
  default boolean contains(NameOccurrence occurrence) {
    return !findDeclaration((DelphiNameOccurrence) occurrence).isEmpty();
  }
}
