package org.sonar.plugins.communitydelphi.api.symbol.scope;

import org.sonar.plugins.communitydelphi.api.type.Typed;

public interface TypeScope extends DelphiScope, Typed {
  DelphiScope getSuperTypeScope();
}
