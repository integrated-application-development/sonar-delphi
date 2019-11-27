package org.sonar.plugins.delphi.symbol;

public class TypeScope extends AbstractDelphiScope {
  private TypeNameDeclaration typeDeclaration;

  public void setTypeDeclaration(TypeNameDeclaration typeDeclaration) {
    this.typeDeclaration = typeDeclaration;
  }

  @Override
  public String toString() {
    return typeDeclaration.getImage() + " <TypeScope>";
  }
}
