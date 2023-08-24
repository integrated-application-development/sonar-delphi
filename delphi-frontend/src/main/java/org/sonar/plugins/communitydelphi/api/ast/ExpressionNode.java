/*
 * Sonar Delphi Plugin
 * Copyright (C) 2023 Integrated Application Development
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
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
