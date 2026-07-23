/*
 * Sonar Delphi Plugin
 * Copyright (C) 2019 Integrated Application Development
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

/**
 * A conditional expression ("inline if"), introduced in Delphi 13.
 *
 * <p>Unlike {@link IfStatementNode}, both the {@code then} and {@code else} branches are mandatory,
 * as the expression must always produce a value. The resulting {@link
 * org.sonar.plugins.communitydelphi.api.type.Type} is the least upper bound (i.e. common type) of
 * the {@code then} and {@code else} expressions' types.
 *
 * <pre>
 *   Result := if Condition then ThenExpression else ElseExpression;
 * </pre>
 *
 * @see <a
 *     href="https://docwiki.embarcadero.com/RADStudio/Florence/en/Conditional_Operators_(Delphi)">
 *     Conditional Operators (Delphi)</a>
 */
public interface IfExpressionNode extends ExpressionNode {
  ExpressionNode getConditionExpression();

  ExpressionNode getThenExpression();

  ExpressionNode getElseExpression();
}
