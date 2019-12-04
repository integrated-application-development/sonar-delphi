package org.sonar.plugins.delphi.symbol.scope;

import javax.annotation.Nullable;

public class MethodScope extends AbstractDelphiScope {

  private final String name;
  private final DelphiScope typeScope;

  public MethodScope(String name, @Nullable DelphiScope typeScope) {
    this.name = name;
    this.typeScope = typeScope;
  }

  @Nullable
  public DelphiScope getTypeScope() {
    return typeScope;
  }

  @Override
  public String toString() {
    return name + " <MethodScope>:" + glomNames(getVariableDeclarations());
  }
}
