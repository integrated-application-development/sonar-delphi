package org.sonar.plugins.communitydelphi.api.symbol.declaration;

import org.sonar.plugins.communitydelphi.api.symbol.Invocable;
import java.util.Set;
import org.jetbrains.annotations.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.Visibility;

public interface MethodNameDeclaration
    extends GenerifiableDeclaration, TypedDeclaration, Invocable, Visibility {
  MethodKind getMethodKind();

  String fullyQualifiedName();

  Set<MethodDirective> getDirectives();

  boolean hasDirective(MethodDirective directive);

  @Nullable
  TypeNameDeclaration getTypeDeclaration();
}
