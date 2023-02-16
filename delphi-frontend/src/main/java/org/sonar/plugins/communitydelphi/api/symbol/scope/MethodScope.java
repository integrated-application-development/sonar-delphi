package org.sonar.plugins.communitydelphi.api.symbol.scope;

import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.MethodNameDeclaration;

public interface MethodScope extends DelphiScope {
  @Nullable
  DelphiScope getTypeScope();

  @Nullable
  MethodNameDeclaration getMethodNameDeclaration();
}
