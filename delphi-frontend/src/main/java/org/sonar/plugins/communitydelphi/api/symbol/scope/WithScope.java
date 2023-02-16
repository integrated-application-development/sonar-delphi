package org.sonar.plugins.communitydelphi.api.symbol.scope;

public interface WithScope extends LocalScope {
  DelphiScope getTargetScope();
}
