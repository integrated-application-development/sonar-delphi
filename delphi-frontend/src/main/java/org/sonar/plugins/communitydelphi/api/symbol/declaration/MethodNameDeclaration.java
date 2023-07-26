package org.sonar.plugins.communitydelphi.api.symbol.declaration;

import java.util.Set;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.ast.Visibility;
import org.sonar.plugins.communitydelphi.api.symbol.Invocable;

public interface MethodNameDeclaration
    extends GenerifiableDeclaration, TypedDeclaration, Invocable, Visibility {
  MethodKind getMethodKind();

  String fullyQualifiedName();

  Set<MethodDirective> getDirectives();

  boolean hasDirective(MethodDirective directive);

  @Nullable
  TypeNameDeclaration getTypeDeclaration();
}
