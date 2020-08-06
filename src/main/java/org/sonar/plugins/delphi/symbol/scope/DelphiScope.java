package org.sonar.plugins.delphi.symbol.scope;

import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;
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
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.HelperType;

public interface DelphiScope extends Scope {

  /**
   * Find declarations in this scope based off of some name occurrence
   *
   * @param occurrence The name for which we are trying to find a matching declaration
   * @return Set of name declarations matching the name occurrence
   */
  Set<NameDeclaration> findDeclaration(DelphiNameOccurrence occurrence);

  /**
   * Find a helper type in this scope based off of some type
   *
   * @param type The type for which we are trying to find a helper
   * @return Helper type for the specified type
   */
  @Nullable
  HelperType getHelperForType(Type type);

  /**
   * If the result set is populated with only Method declarations that are marked as overloads, then
   * additional overloads will be searched for and populated into the result set.
   *
   * @param occurrence The name occurrence that we're accumulating declarations for
   * @param result The set of declarations that overloads will be added to, if applicable
   */
  void findMethodOverloads(DelphiNameOccurrence occurrence, Set<NameDeclaration> result);

  @Nullable
  @Override
  DelphiScope getParent();

  List<NameOccurrence> getOccurrencesFor(NameDeclaration declaration);

  Set<NameDeclaration> getAllDeclarations();

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

  static UnknownScope unknownScope() {
    return UnknownScope.instance();
  }
}
