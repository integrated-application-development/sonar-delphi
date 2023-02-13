package org.sonar.plugins.communitydelphi.api.ast;

import au.com.integradev.delphi.type.Type;
import au.com.integradev.delphi.type.Typed;
import javax.annotation.Nonnull;

public interface ConstDeclarationNode extends DelphiNode, Typed, Visibility {
  NameDeclarationNode getNameDeclarationNode();

  ExpressionNode getExpression();

  TypeNode getTypeNode();

  @Override
  @Nonnull
  Type getType();

  @Override
  VisibilityType getVisibility();
}
