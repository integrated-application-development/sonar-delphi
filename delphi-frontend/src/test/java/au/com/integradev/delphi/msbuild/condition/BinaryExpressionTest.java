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
package au.com.integradev.delphi.msbuild.condition;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import au.com.integradev.delphi.msbuild.condition.Token.TokenType;
import au.com.integradev.delphi.msbuild.expression.ExpressionEvaluator;
import org.junit.jupiter.api.Test;

class BinaryExpressionTest {
  @Test
  void testInvalidOperatorShouldThrow() {
    BinaryExpression expression =
        new BinaryExpression(
            new StringExpression("foo", false),
            TokenType.END_OF_INPUT,
            new StringExpression("bar", false));

    ExpressionEvaluator evaluator = mock(ExpressionEvaluator.class);
    when(evaluator.eval(anyString())).thenAnswer(string -> string);

    assertThatThrownBy(() -> expression.boolEvaluate(evaluator))
        .isInstanceOf(InvalidExpressionException.class);
  }
}
