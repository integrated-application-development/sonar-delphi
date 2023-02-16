package org.sonar.plugins.communitydelphi.api.symbol.declaration;

import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.scope.FileScope;

public interface UnitImportNameDeclaration extends QualifiedNameDeclaration {
  @Nullable
  UnitNameDeclaration getOriginalDeclaration();

  @Nullable
  FileScope getUnitScope();
}
