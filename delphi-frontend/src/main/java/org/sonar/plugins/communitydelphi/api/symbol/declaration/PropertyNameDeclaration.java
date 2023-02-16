package org.sonar.plugins.communitydelphi.api.symbol.declaration;

import org.sonar.plugins.communitydelphi.api.symbol.Invocable;
import java.util.List;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.Visibility;

public interface PropertyNameDeclaration extends TypedDeclaration, Invocable, Visibility {
  String fullyQualifiedName();

  @Nullable
  NameDeclaration getReadDeclaration();

  @Nullable
  NameDeclaration getWriteDeclaration();

  boolean isArrayProperty();

  boolean isDefaultProperty();

  List<PropertyNameDeclaration> getRedeclarations();
}
