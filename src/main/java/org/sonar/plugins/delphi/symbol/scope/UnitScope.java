package org.sonar.plugins.delphi.symbol.scope;

public class UnitScope extends AbstractFileScope {
  private final SystemScope systemScope;

  public UnitScope(String name, SystemScope systemScope, SysInitScope sysInitScope) {
    super(name);
    this.systemScope = systemScope;
    addImport(systemScope);
    addImport(sysInitScope);
  }

  @Override
  public SystemScope getSystemScope() {
    return systemScope;
  }

  @Override
  public String toString() {
    return getName() + "<UnitScope>";
  }
}
