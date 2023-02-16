package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.type.Typed;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ExceptItemNode extends DelphiNode, Typed {
  @Nullable
  NameDeclarationNode getExceptionName();

  @Nonnull
  TypeReferenceNode getExceptionType();

  @Nullable
  StatementNode getStatement();
}
