package org.sonar.plugins.delphi.symbol;

import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.symboltable.Scope;

public interface DelphiScope extends Scope {
  default Map<UnitNameDeclaration, List<NameOccurrence>> getUnitDeclarations() {
    return getDeclarations(UnitNameDeclaration.class);
  }

  default Map<UnitImportNameDeclaration, List<NameOccurrence>> getImportDeclarations() {
    return getDeclarations(UnitImportNameDeclaration.class);
  }

  default Map<TypeNameDeclaration, List<NameOccurrence>> getTypeDeclarations() {
    return getDeclarations(TypeNameDeclaration.class);
  }

  default Map<PropertyNameDeclaration, List<NameOccurrence>> getPropertyDeclarations() {
    return getDeclarations(PropertyNameDeclaration.class);
  }

  default Map<MethodNameDeclaration, List<NameOccurrence>> getMethodDeclarations() {
    return getDeclarations(MethodNameDeclaration.class);
  }

  default Map<VariableNameDeclaration, List<NameOccurrence>> getVariableDeclarations() {
    return getDeclarations(VariableNameDeclaration.class);
  }

  Set<NameDeclaration> findDeclaration(DelphiNameOccurrence occurrence);

  void findMethodOverloads(DelphiNameOccurrence occurrence, Set<NameDeclaration> result);

  @Override
  DelphiScope getParent();
}
