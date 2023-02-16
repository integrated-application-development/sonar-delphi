package org.sonar.plugins.communitydelphi.api.symbol.scope;

import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;

public interface SystemScope extends FileScope {
  TypeNameDeclaration getTObjectDeclaration();

  TypeNameDeclaration getIInterfaceDeclaration();

  TypeNameDeclaration getTVarRecDeclaration();

  TypeNameDeclaration getTClassHelperBaseDeclaration();
}
