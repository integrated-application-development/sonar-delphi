package org.sonar.plugins.delphi.symbol.scope;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;

import com.google.errorprone.annotations.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.symboltable.Scope;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;
import org.sonar.plugins.delphi.type.Type;
import org.sonar.plugins.delphi.type.Type.HelperType;

@Immutable
public final class UnknownScope implements DelphiScope {
  private static final UnknownScope UNKNOWN_SCOPE = new UnknownScope();

  private UnknownScope() {
    // Hide constructor
  }

  static UnknownScope instance() {
    return UNKNOWN_SCOPE;
  }

  @Override
  public <T extends Scope> T getEnclosingScope(Class<T> clazz) {
    return null;
  }

  @Override
  public Map<NameDeclaration, List<NameOccurrence>> getDeclarations() {
    return Collections.emptyMap();
  }

  @Override
  public <T extends NameDeclaration> Map<T, List<NameOccurrence>> getDeclarations(Class<T> clazz) {
    return null;
  }

  @Override
  public boolean contains(NameOccurrence nameOccurrence) {
    return false;
  }

  @Override
  public Set<NameDeclaration> addNameOccurrence(NameOccurrence nameOccurrence) {
    return emptySet();
  }

  @Override
  public Set<NameDeclaration> findDeclaration(DelphiNameOccurrence occurrence) {
    return emptySet();
  }

  @Override
  public void addDeclaration(NameDeclaration nameDeclaration) {
    // Do nothing
  }

  @Override
  public void findMethodOverloads(DelphiNameOccurrence occurrence, Set<NameDeclaration> result) {
    // Do nothing
  }

  @Override
  public DelphiScope getParent() {
    return null;
  }

  @Override
  public void setParent(Scope scope) {
    // Do nothing
  }

  @Override
  public Set<NameDeclaration> getAllDeclarations() {
    return emptySet();
  }

  @Override
  public <T extends NameDeclaration> Set<T> getDeclarationSet(Class<T> clazz) {
    return emptySet();
  }

  @Override
  public List<NameOccurrence> getOccurrencesFor(NameDeclaration declaration) {
    return emptyList();
  }

  @Nullable
  @Override
  public HelperType getHelperForType(Type type) {
    return null;
  }
}
