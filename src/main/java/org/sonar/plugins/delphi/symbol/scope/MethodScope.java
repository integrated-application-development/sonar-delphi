package org.sonar.plugins.delphi.symbol.scope;

import javax.annotation.Nullable;

public class MethodScope extends AbstractDelphiScope {
  private final String name;
  private DelphiScope typeScope;

  public MethodScope(String name) {
    this.name = name;
  }

  @Nullable
  public DelphiScope getTypeScope() {
    return typeScope;
  }

  public void setTypeScope(DelphiScope typeScope) {
    this.typeScope = typeScope;
  }

  @Override
  public String toString() {
    return name + " <MethodScope>:" + glomNames(getVariableDeclarations());
  }
}
