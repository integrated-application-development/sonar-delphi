package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nonnull;
import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Typed;

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
