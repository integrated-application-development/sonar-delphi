package org.sonar.plugins.delphi.symbol.scope;

public class SysInitScope extends AbstractFileScope {
  private final SystemScope systemScope;

  public SysInitScope(String name, SystemScope systemScope) {
    super(name);
    this.systemScope = systemScope;
    addImport(systemScope);
  }

  @Override
  public SystemScope getSystemScope() {
    return systemScope;
  }
}
