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

/** {@code try} statement. */
public interface TryStatementNode extends StatementNode {
  /**
   * Returns the list of statements from within this {@code try} statement.
   *
   * @return statement list from within this {@code try} statement
   */
  StatementListNode getStatementList();

  /**
   * Returns whether this {@code try} statement has an {@code except} block.
   *
   * @return true if this {@code try} statement has an {@code except} block
   */
  boolean hasExceptBlock();

  /**
   * Returns whether this {@code try} statement has a {@code finally} block.
   *
   * @return true if this {@code try} statement has a {@code finally} block
   */
  boolean hasFinallyBlock();

  /**
   * Returns the {@code except} block for this {@code try} statement.
   *
   * @return the {@code except} block for this {@code try} statement
   * @deprecated Use {@link TryStatementNode#getExceptBlock()} instead
   */
  @Deprecated(forRemoval = true)
  ExceptBlockNode getExpectBlock();

  /**
   * Returns the {@code except} block for this {@code try} statement.
   *
   * @return the {@code except} block for this {@code try} statement
   */
  ExceptBlockNode getExceptBlock();

  /**
   * Returns the {@code finally} block for this {@code try} statement.
   *
   * @return the {@code finally} block for this {@code try} statement
   */
  FinallyBlockNode getFinallyBlock();
}
