package org.sonar.plugins.delphi.symbol.scope;

public class WithScope extends LocalScope {
  private final DelphiScope targetScope;

  public WithScope(DelphiScope targetScope) {
    this.targetScope = targetScope;
  }

  public DelphiScope getTargetScope() {
    return targetScope;
  }

  @Override
  public String toString() {
    return String.format("WithScope (Target: %s)", targetScope);
  }
}
