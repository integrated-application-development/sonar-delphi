package org.sonar.plugins.communitydelphi.api.ast;

import org.sonar.plugins.communitydelphi.api.type.Type;
import org.sonar.plugins.communitydelphi.api.type.Typed;
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
