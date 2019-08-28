package org.sonar.plugins.delphi.symbol;

import com.google.errorprone.annotations.Immutable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import net.sourceforge.pmd.lang.symboltable.NameOccurrence;
import net.sourceforge.pmd.lang.symboltable.Scope;

@Immutable
public final class UnknownScope implements DelphiScope {
  private static final UnknownScope UNKNOWN_SCOPE = new UnknownScope();

  private UnknownScope() {
    // Hide constructor
  }

  public static UnknownScope unknownScope() {
    return UNKNOWN_SCOPE;
  }

  @Override
  public <T extends Scope> T getEnclosingScope(Class<T> clazz) {
    return null;
  }

  @Override
  public Map<NameDeclaration, List<NameOccurrence>> getDeclarations() {
    return null;
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
    return Collections.emptySet();
  }

  @Override
  public Set<NameDeclaration> findDeclaration(DelphiNameOccurrence occurrence) {
    return Collections.emptySet();
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
  public void addDeclaration(NameDeclaration nameDeclaration) {
    // Do nothing
  }

  @Override
  public void findMethodOverloads(DelphiNameOccurrence occurrence, Set<NameDeclaration> result) {
    // Do nothing
  }
}
