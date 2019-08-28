package org.sonar.plugins.delphi.symbol;

import java.util.HashSet;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;

public class GlobalScope extends AbstractDelphiScope {
  @Override
  public String toString() {
    return "<GlobalScope>";
  }

  @Override
  public Set<NameDeclaration> findDeclaration(DelphiNameOccurrence occurrence) {
    Set<NameDeclaration> result = new HashSet<>();
    searchDeclarations(occurrence, getUnitDeclarations(), result);
    return result;
  }
}
