package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.symbol.NameDeclaration;
import au.com.integradev.delphi.symbol.Qualifiable;
import javax.annotation.Nullable;

public interface TypeReferenceNode extends TypeNode, Qualifiable {
  @Nullable
  NameDeclaration getTypeDeclaration();

  NameReferenceNode getNameNode();
}
