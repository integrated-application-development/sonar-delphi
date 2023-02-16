package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.symbol.Qualifiable;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.symbol.declaration.NameDeclaration;

public interface TypeReferenceNode extends TypeNode, Qualifiable {
  @Nullable
  NameDeclaration getTypeDeclaration();

  NameReferenceNode getNameNode();
}
