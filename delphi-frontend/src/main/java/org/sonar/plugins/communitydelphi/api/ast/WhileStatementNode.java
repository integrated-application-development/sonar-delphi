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

import javax.annotation.Nullable;

/** {@code while} loop statement. */
public interface WhileStatementNode extends StatementNode {
  /**
   * Returns the guard expression for this {@code while} loop.
   *
   * @return the guard expression for this {@code while} loop
   */
  ExpressionNode getGuardExpression();

  /**
   * Returns the statement that will be executed in this {@code while} loop, also known as the loop
   * body.
   *
   * @return the statement that will be executed in this {@code while} loop
   */
  @Nullable
  StatementNode getStatement();
}
