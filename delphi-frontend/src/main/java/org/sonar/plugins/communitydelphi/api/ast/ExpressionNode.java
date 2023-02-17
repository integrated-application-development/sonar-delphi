package org.sonar.plugins.communitydelphi.api.ast;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.sonar.plugins.communitydelphi.api.type.Typed;

public interface ExpressionNode extends DelphiNode, Typed {
  boolean isLiteral();

  boolean isIntegerLiteral();

  boolean isHexadecimalLiteral();

  boolean isBinaryLiteral();

  boolean isStringLiteral();

  boolean isRealLiteral();

  boolean isNilLiteral();

  boolean isBooleanLiteral();

  boolean isTrue();

  boolean isFalse();

  boolean isResult();

  boolean isReferenceTo(String image);

  @Nullable
  LiteralNode extractLiteral();

  @Nullable
  NameReferenceNode extractSimpleNameReference();

  /**
   * Traverses nested parenthesized expressions and returns the expression inside of them
   *
   * @return Nested expression
   */
  @Nonnull
  ExpressionNode skipParentheses();

  /**
   * From a potentially nested expression, traverses any parenthesized expressions ancestors and
   * returns the top-level expression. The reverse of {@link ExpressionNode#skipParentheses}
   *
   * @return Top-level expression
   */
  @Nonnull
  ExpressionNode findParentheses();
}
