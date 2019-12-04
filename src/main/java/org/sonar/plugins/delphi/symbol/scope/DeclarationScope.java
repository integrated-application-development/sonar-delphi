package org.sonar.plugins.delphi.symbol.scope;

import java.util.Collections;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;
import org.sonar.plugins.delphi.symbol.DelphiNameOccurrence;

public class DeclarationScope extends AbstractDelphiScope {
  @Override
  public Set<NameDeclaration> findDeclaration(DelphiNameOccurrence occurrence) {
    return Collections.emptySet();
  }
}
