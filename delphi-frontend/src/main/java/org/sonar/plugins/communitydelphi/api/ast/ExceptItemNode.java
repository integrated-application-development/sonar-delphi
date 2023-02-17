package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.type.Typed;

public interface ExceptItemNode extends DelphiNode, Typed {
  @Nullable
  NameDeclarationNode getExceptionName();

  @Nonnull
  TypeReferenceNode getExceptionType();

  @Nullable
  StatementNode getStatement();
}
