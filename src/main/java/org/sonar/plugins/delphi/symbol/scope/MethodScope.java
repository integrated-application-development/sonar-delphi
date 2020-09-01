package org.sonar.plugins.delphi.symbol.scope;

import javax.annotation.Nullable;

public class MethodScope extends AbstractDelphiScope {
  private DelphiScope typeScope;

  @Nullable
  public DelphiScope getTypeScope() {
    return typeScope;
  }

  public void setTypeScope(DelphiScope typeScope) {
    this.typeScope = typeScope;
  }

  @Override
  public String toString() {
    return "<MethodScope>:" + glomNames(getVariableDeclarations());
  }
}
