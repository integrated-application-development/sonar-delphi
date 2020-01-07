package org.sonar.plugins.delphi.symbol.scope;

public class TypeScope extends AbstractDelphiScope {
  private final String typeName;
  private DelphiScope superTypeScope;

  public TypeScope(String typeName) {
    this.typeName = typeName;
  }

  public TypeScope() {
    this("(anonymous type)");
  }

  public void setSuperTypeScope(DelphiScope superTypeScope) {
    this.superTypeScope = superTypeScope;
  }

  public DelphiScope getSuperTypeScope() {
    return superTypeScope;
  }

  @Override
  public String toString() {
    return typeName + " <TypeScope>";
  }
}
