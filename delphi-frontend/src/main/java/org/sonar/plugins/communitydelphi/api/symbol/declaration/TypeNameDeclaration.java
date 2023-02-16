package org.sonar.plugins.communitydelphi.api.symbol.declaration;

import javax.annotation.Nullable;

public interface TypeNameDeclaration extends GenerifiableDeclaration, TypedDeclaration {
  String fullyQualifiedName();

  @Nullable
  TypeNameDeclaration getAliased();
}
