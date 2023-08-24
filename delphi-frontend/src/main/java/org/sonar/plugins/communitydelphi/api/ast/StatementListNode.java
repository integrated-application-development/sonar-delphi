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

import java.util.List;
import java.util.stream.Stream;

/**
 * Statement Lists are effectively implicit compound statements that Delphi uses in a few places.
 * The StatementListNode is not itself a statement.
 *
 * <p>In terms of file position, Statement Lists are entirely abstract, meaning they have an
 * imaginary token at their root and can also be empty. In light of this, StatementListNodes will
 * always return their parents file position.
 *
 * <p>Examples:
 *
 * <ul>
 *   <li>{@code try {statementList} except {statementList} end}
 *   <li>{@code repeat {statementList} until {expression}}
 * </ul>
 */
public interface StatementListNode extends DelphiNode {

  boolean isEmpty();

  List<StatementNode> getStatements();

  List<StatementNode> getDescendantStatements();

  Stream<StatementNode> statementStream();

  Stream<StatementNode> descendantStatementStream();
}
