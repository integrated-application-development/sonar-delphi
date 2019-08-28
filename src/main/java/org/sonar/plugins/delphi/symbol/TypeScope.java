package org.sonar.plugins.delphi.symbol;

import java.util.Collections;
import java.util.Set;
import net.sourceforge.pmd.lang.symboltable.NameDeclaration;

public class TypeScope extends AbstractDelphiScope {
  private final TypeNameDeclaration typeDeclaration;

  public TypeScope(final TypeNameDeclaration typeNameDeclaration) {
    this.typeDeclaration = typeNameDeclaration;
  }

  @Override
  public Set<NameDeclaration> findDeclaration(DelphiNameOccurrence occurrence) {
    if (occurrence.isSelf() || typeDeclaration.getImage().equalsIgnoreCase(occurrence.getImage())) {
      return Collections.singleton(typeDeclaration);
    }

    return super.findDeclaration(occurrence);
  }

  @Override
  public String toString() {
    return typeDeclaration.getImage() + " <TypeScope>";
  }
}
