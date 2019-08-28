package org.sonar.plugins.delphi.symbol;

import java.util.Collections;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;

public class DeclarationScope extends AbstractDelphiScope {
  @Override
  public Set<NameDeclaration> findDeclaration(DelphiNameOccurrence occurrence) {
    return Collections.emptySet();
  }
}
