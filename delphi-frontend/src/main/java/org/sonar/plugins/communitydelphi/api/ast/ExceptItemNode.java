package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.type.Typed;
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
