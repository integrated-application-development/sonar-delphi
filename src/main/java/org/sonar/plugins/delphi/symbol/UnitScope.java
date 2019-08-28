package org.sonar.plugins.delphi.symbol;

public class UnitScope extends AbstractDelphiScope {
  @Override
  public String toString() {
    return "<UnitScope>:" + glomNames(getDeclarations().keySet());
  }
}
