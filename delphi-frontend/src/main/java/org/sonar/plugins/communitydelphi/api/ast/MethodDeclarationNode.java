package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.TypeNameDeclaration;

public interface MethodDeclarationNode extends MethodNode, Visibility {
  boolean isOverride();

  boolean isVirtual();

  boolean isMessage();

  @Nullable
  TypeNameDeclaration getTypeDeclaration();
}
